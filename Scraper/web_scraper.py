import requests
from bs4 import BeautifulSoup
import pandas as pd
import time

def scrape_prices_and_streets(base_url, pages=2):
    results = []

    for page in range(1, pages + 1):
        print(f"Downloading page {page}...")

        url = f"{base_url}&page={page}"
        response = requests.get(url, headers={
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"
        })
        response.encoding = "utf-8"

        if response.status_code != 200:
            print(f"Error downloading page {page}")
            continue

        soup = BeautifulSoup(response.content, 'html.parser')

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

        for street, price, (size, rooms, floor) in zip(streets, prices, property_infos):
            results.append({
                "street": street,
                "price_pln": price,
                "size_m2": size,
                "rooms": rooms,
                "floor": floor
            })

        time.sleep(1)

    return results

BASE_URL = "https://www.morizon.pl/mieszkania/gdansk/?ps%5Blocation%5D%5Bmap%5D=1"
data = scrape_prices_and_streets(BASE_URL)

df = pd.DataFrame(data)
df.to_csv("morizon_data.csv", index=False, encoding="utf-8-sig")
print("Saved to morizon_data.csv")
