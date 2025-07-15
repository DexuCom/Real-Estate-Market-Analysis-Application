import asyncio
import aiohttp
from bs4 import BeautifulSoup
import pandas as pd
import time
import json
import lxml
import os

HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"
}

async def fetch(session, url):
    async with session.get(url, headers=HEADERS) as response:
        return await response.text()

def parse_page(html):
    soup = BeautifulSoup(html, 'lxml')

    price_divs = soup.find_all("div", attrs={"data-v-3ad00e2f": ""})
    street_spans = soup.find_all("span", attrs={"class": "_4x2Yz-", "data-v-c3fd043d": ""})
    info_divs = soup.find_all("div", class_="property-info")
    offer_links = soup.find_all("a", class_="iKasml")

    prices = []
    for div in price_divs:
        text = div.get_text(strip=True)
        if "zł" in text:
            numeric_price = text.replace("zł", "").replace(" ", "").replace("\xa0", "")
            try:
                price = int(numeric_price)
                prices.append(price)
            except ValueError:
                continue

    streets = [span.get_text(strip=True) for span in street_spans]

    property_infos = []
    for info_div in info_divs:
        text = info_div.get_text(separator=" ", strip=True)
        parts = text.split("•")
        size = parts[0].strip() if len(parts) > 0 else None
        rooms = parts[1].strip() if len(parts) > 1 else None
        floor = parts[2].strip() if len(parts) > 2 else None
        property_infos.append((size, rooms, floor))

    base_detail_url = "https://www.morizon.pl"
    detail_links = [base_detail_url + a['href'] for a in offer_links if a.get('href')]

    results = []
    for street, price, (size, rooms, floor), detail_url in zip(streets, prices, property_infos, detail_links):
        results.append({
            "street": street,
            "price_pln": price,
            "size_m2": size,
            "rooms": rooms,
            "floor": floor,
            "detail_url": detail_url
        })

    return results

def parse_year_built(html):
    soup = BeautifulSoup(html, 'lxml')
    span = soup.find("span", string="Rok budowy")
    if span:
        parent_div = span.find_parent("div", class_="iT04N1")
        if parent_div:
            value_div = parent_div.find("div", class_="YSTCwm M3ijI0")
            if value_div:
                year_div = value_div.find("div", attrs={"data-cy": "itemValue"})
                if year_div:
                    year_text = year_div.get_text(strip=True)
                    if year_text.isdigit():
                        return int(year_text)
    return -1

def parse_coords(html):
    soup = BeautifulSoup(html, 'lxml')
    script = soup.find("script", string=lambda s: s and "window.__INITIAL_STATE__" in s)
    if script:
        try:
            start = script.string.find('window.__INITIAL_STATE__ = JSON.stringify(')
            if start != -1:
                json_start = script.string.find('{', start)
                json_end = script.string.rfind('}')
                json_str = script.string[json_start:json_end+1]
                data = json.loads(json_str)
                coords = data.get("coords", None)
                return coords
        except Exception:
            pass
    return None

async def scrape_prices_and_streets(base_url, pages=1):
    results = []
    async with aiohttp.ClientSession() as session:
        tasks = []
        for page in range(1, pages + 1):
            url = f"{base_url}&page={page}"
            tasks.append(fetch(session, url))

        start_time = time.perf_counter()
        pages_html = await asyncio.gather(*tasks)

        for i, html in enumerate(pages_html, start=1):
            print(f"Processing page {i}...")
            page_results = parse_page(html)
            results.extend(page_results)

        print("Fetching year built for each offer...")
        detail_tasks = [fetch(session, item["detail_url"]) for item in results]
        detail_htmls = await asyncio.gather(*detail_tasks)

        for item, detail_html in zip(results, detail_htmls):
            year_built = parse_year_built(detail_html)
            item["year_built"] = year_built

        print("Fetching coordinates for each offer...")
        coords_tasks = [fetch(session, item["detail_url"] + "/analiza") for item in results]
        coords_htmls = await asyncio.gather(*coords_tasks)

        for item, coords_html in zip(results, coords_htmls):
            coords = parse_coords(coords_html)
            item["coords"] = coords if coords else ""

        end_time = time.perf_counter()

        total_time = end_time - start_time
        total_ads = len(results)
        speed = total_ads / total_time if total_time > 0 else 0

        print(f"\nDownloaded {total_ads} ads in {total_time:.2f} seconds.")
        print(f"Average speed: {speed:.2f} ads per second.")

    return results

if __name__ == "__main__":
    output_dir = "ScraperOutput"
    os.makedirs(output_dir, exist_ok=True)
    
    BASE_URL = "https://www.morizon.pl/mieszkania/gdansk/?ps%5Blocation%5D%5Bmap%5D=1"
    data = asyncio.run(scrape_prices_and_streets(BASE_URL, pages=10))

    output_file = os.path.join(output_dir, "morizon_data.csv")
    df = pd.DataFrame(data)
    df.to_csv(output_file, index=False, encoding="utf-8-sig")
    print(f"Data saved to {output_file}")
