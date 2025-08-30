import asyncio
from pydoc import text
import aiohttp
from bs4 import BeautifulSoup
import pandas as pd
import time
import json
import lxml
import os
import re

HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"
}

DOMOFON_RE = re.compile(r"\bdomofon\b", re.IGNORECASE)
PIWNICA_RE = re.compile(r"\bpiwnica\b", re.IGNORECASE)
MEBLE_RE = re.compile(r"\bmeble\b", re.IGNORECASE)
WINDA_RE = re.compile(r"\bwinda\b", re.IGNORECASE)
MIEJSCE_POSTOJOWE_RE = re.compile(r"miejsce\s+postojowe", re.IGNORECASE)
OBIEKT_ZAMKNIETY_RE = re.compile(r"\bobiekt zamknięty\b", re.IGNORECASE)

async def fetch(session, url):
    async with session.get(url, headers=HEADERS) as response:
        return await response.text()

def parse_page(html):
    soup = BeautifulSoup(html, 'lxml')

    price_divs = soup.find_all("div", attrs={"data-v-3ad00e2f": ""})
    street_spans = soup.find_all("span", attrs={"class": "repT6T", "data-v-8fbe0482": ""})
    info_divs = soup.find_all("div", class_="property-info")
    offer_links = soup.find_all("a", class_="_4izXSr")
    image_divs = soup.find_all("div", class_="gallery-slider__img-wrapper gallery-slider__img-wrapper")

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

    image_links = []
    for div in image_divs:
        img = div.find("img")
        if img and img.get("src"):
            image_links.append(img.get("src"))
        else:
            image_links.append("")

    base_detail_url = "https://www.morizon.pl"
    detail_links = [base_detail_url + a['href'] for a in offer_links if a.get('href')]

    results = []
    for street, price, (size, rooms, floor), detail_url, image_link in zip(streets, prices, property_infos, detail_links, image_links):
        results.append({
            "city": SELECTED_CITY,
            "street": street,
            "price_pln": price,
            "size_m2": size,
            "rooms": rooms,
            "floor": floor,
            "detail_url": detail_url,
            "image_url": image_link
        })

    return results


def parse_detail_value(soup: BeautifulSoup, label_text: str):
    span = soup.find("span", string=label_text)
    if span:
        parent_div = span.find_parent("div", class_="iT04N1")
        if parent_div:
            value_div = parent_div.find("div", class_="YSTCwm M3ijI0")
            if value_div:
                val_div = value_div.find("div", attrs={"data-cy": "itemValue"})
                if val_div:
                    return val_div.get_text(strip=True)
    return None


def parse_year_built(soup: BeautifulSoup):
    text = parse_detail_value(soup, "Rok budowy")
    return int(text) if text and text.isdigit() else -1


def parse_market(soup: BeautifulSoup):
    text = parse_detail_value(soup, "Rynek")
    return text.strip().lower() if text else ""


def parse_heating(soup: BeautifulSoup):
    text = parse_detail_value(soup, "Ogrzewanie")
    return text if text else "-1"

def parse_balcony(soup: BeautifulSoup) -> int:
    text = parse_detail_value(soup, "Balkon")
    if not text:
        return 0
    t = text.strip().lower()
    if t == "tak":
        return 1
    if t == "nie":
        return 0
    return 0


def parse_terrace(soup: BeautifulSoup) -> int:
    text = parse_detail_value(soup, "Taras")
    if not text:
        return 0
    t = text.strip().lower()
    if t == "tak":
        return 1
    if t == "nie":
        return 0
    return 0


def parse_garden(soup: BeautifulSoup) -> int:
    text = parse_detail_value(soup, "Ogród")
    if not text:
        return 0
    t = text.strip().lower()
    if t == "tak":
        return 1
    if t == "nie":
        return 0
    return 0

def parse_total_floors(soup: BeautifulSoup) -> int:
    text = parse_detail_value(soup, "Liczba pięter")
    if not text:
        return -1
    m = re.search(r"\d+", text)
    return int(m.group(0)) if m else -1


def parse_boolean_feature(soup: BeautifulSoup, pattern: re.Pattern) -> int:
    page_text = soup.get_text(separator=" ", strip=True)
    return 1 if pattern.search(page_text) else 0

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
            progress = (i / pages) * 100
            print(f"Processing page {i}/{pages} ({progress:.1f}%)...")
            page_results = parse_page(html)
            results.extend(page_results)

        print("Fetching year built for each offer...")
        detail_tasks = [fetch(session, item["detail_url"]) for item in results]
        detail_htmls = await asyncio.gather(*detail_tasks)

        total_offers = len(results)
        for i, (item, detail_html) in enumerate(zip(results, detail_htmls), start=1):
            progress = (i / total_offers) * 100
            if i % max(1, total_offers // 10) == 0 or i == total_offers:
                print(f"Fetching year built: {i}/{total_offers} ({progress:.1f}%)")
            detail_soup = BeautifulSoup(detail_html, 'lxml')
            year_built = parse_year_built(detail_soup)
            item["year_built"] = year_built
            market = parse_market(detail_soup)
            item["market"] = market
            heating = parse_heating(detail_soup)
            item["heating"] = heating
            total_floors = parse_total_floors(detail_soup)
            item["total_floors"] = total_floors
            item["Intercom"] = parse_boolean_feature(detail_soup, DOMOFON_RE)
            item["Basement"] = parse_boolean_feature(detail_soup, PIWNICA_RE)
            item["Furnished"] = parse_boolean_feature(detail_soup, MEBLE_RE)
            item["Elevator"] = parse_boolean_feature(detail_soup, WINDA_RE)
            item["Parking space"] = parse_boolean_feature(detail_soup, MIEJSCE_POSTOJOWE_RE)
            item["Gated property"] = parse_boolean_feature(detail_soup, OBIEKT_ZAMKNIETY_RE)
            item["balcony"] = parse_balcony(detail_soup)
            item["terrace"] = parse_terrace(detail_soup)
            item["garden"] = parse_garden(detail_soup)


        print("Fetching coordinates for each offer...")
        coords_tasks = [fetch(session, item["detail_url"] + "/analiza") for item in results]
        coords_htmls = await asyncio.gather(*coords_tasks)

        for i, (item, coords_html) in enumerate(zip(results, coords_htmls), start=1):
            progress = (i / total_offers) * 100
            if i % max(1, total_offers // 10) == 0 or i == total_offers:
                print(f"Fetching coordinates: {i}/{total_offers} ({progress:.1f}%)")
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
    CITIES = {
        "Gdańsk": "https://www.morizon.pl/mieszkania/gdansk/?ps%5Blocation%5D%5Bmap%5D=1",
        "Warszawa": "https://www.morizon.pl/mieszkania/warszawa/?ps%5Blocation%5D%5Bmap%5D=1",
        "Kraków": "https://www.morizon.pl/mieszkania/krakow/?ps%5Blocation%5D%5Bmap%5D=1",
    }

    SELECTED_CITY = "Gdańsk"

    BASE_URL = CITIES[SELECTED_CITY]
    print(f"Starting scraper for {SELECTED_CITY.upper()}...")
    
    output_dir = "ScraperOutput"
    os.makedirs(output_dir, exist_ok=True)
    
    data = asyncio.run(scrape_prices_and_streets(BASE_URL, pages=2))

    output_file = os.path.join(output_dir, f"{SELECTED_CITY}-morizon.csv")
    df = pd.DataFrame(data)
    df.to_csv(output_file, index=False, encoding="utf-8-sig")
    print(f"Data saved to {output_file}")

