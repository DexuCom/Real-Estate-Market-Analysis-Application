import asyncio
import aiohttp
from bs4 import BeautifulSoup
import pandas as pd
import time

HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"
}

async def fetch(session, url):
    async with session.get(url, headers=HEADERS) as response:
        return await response.text()

def parse_page(html):
    soup = BeautifulSoup(html, 'html.parser')

    price_divs = soup.find_all("div", attrs={"data-v-3ad00e2f": ""})
    street_spans = soup.find_all("span", attrs={"class": "_4x2Yz-", "data-v-c3fd043d": ""})
    info_divs = soup.find_all("div", class_="property-info")

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

    results = []
    for street, price, (size, rooms, floor) in zip(streets, prices, property_infos):
        results.append({
            "street": street,
            "price_pln": price,
            "size_m2": size,
            "rooms": rooms,
            "floor": floor
        })

    return results

async def scrape_prices_and_streets(base_url, pages=10):
    results = []
    async with aiohttp.ClientSession() as session:
        tasks = []
        for page in range(1, pages + 1):
            url = f"{base_url}&page={page}"
            tasks.append(fetch(session, url))

        start_time = time.perf_counter()
        pages_html = await asyncio.gather(*tasks)
        end_time = time.perf_counter()

        for i, html in enumerate(pages_html, start=1):
            print(f"Processing page {i}...")
            page_results = parse_page(html)
            results.extend(page_results)

        total_time = end_time - start_time
        total_ads = len(results)
        speed = total_ads / total_time if total_time > 0 else 0

        print(f"\nDownloaded {total_ads} ads in {total_time:.2f} seconds.")
        print(f"Average speed: {speed:.2f} ads per second.")

    return results

if __name__ == "__main__":
    BASE_URL = "https://www.morizon.pl/mieszkania/gdansk/?ps%5Blocation%5D%5Bmap%5D=1"
    data = asyncio.run(scrape_prices_and_streets(BASE_URL, pages=10))

    df = pd.DataFrame(data)
    df.to_csv("morizon_data.csv", index=False, encoding="utf-8-sig")
    print("Data saved to morizon_data.csv")
