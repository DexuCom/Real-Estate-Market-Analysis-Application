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

CENA_RE = re.compile(r"")

async def fetch(session, url):
    async with session.get(url, headers=HEADERS) as response:
        return await response.text()

def parse_page(html):
    soup = BeautifulSoup(html, 'lxml')

    offer_cards = soup.find_all("div", class_="card__outer")
    base_detail_url = "https://www.morizon.pl"

    results = []

    for offer_card in offer_cards:
        street = None
        price = None
        size = None
        floor = None
        detail_url = None
        image_url = None

        link_tag = offer_card.find("a", attrs={"data-cy": "propertyUrl"})
        if not link_tag:
            print("Offer didn't have a link!")
            continue
        detail_url = base_detail_url + link_tag['href']

        price_holder = offer_card.find("div", attrs={"data-cy": "cardPropertyOfferPrice"})
        if price_holder:
            # robimy to w ten sposób bo nie ma sensownego selektora,
            # być może lepiej regexem ze zł wydobyć?
            price_element = price_holder.select_one('div:nth-of-type(2) > div')
            if price_element:
                price_string = price_element.get_text(strip=True)
                numeric_price = price_string.replace("zł", "").replace(" ", "").replace("\xa0", "")
                try:
                    price = int(numeric_price)
                except ValueError:
                    print("Error when trying to convert numeric_price to price, price was " + numeric_price)
                    continue

        location_holder = offer_card.find("div", attrs={"data-cy": "locationTree"})
        if location_holder:
            street_element = location_holder.find_next_sibling('span')
            if street_element:
                street = street_element.get_text(strip=True)

        info_div = offer_card.find("div", class_="property-info")
        if info_div:
            text = info_div.get_text(separator=" ", strip=True)
            parts = text.split("•")
            if len(parts) > 0:
                size_text = parts[0].strip().replace("m²", "").replace(",", "")
                try:
                    size = float(size_text)
                except ValueError:
                    print("Unable to convert size " + size_text + " into a float")

            if len(parts) > 1:
                room_count = re.search(r'\d+', parts[1].strip()).group(0)
                rooms = int(room_count)

            if len(parts) > 2:
                floor_text = parts[2].strip().lower()
                if "parter" in floor_text:
                    floor = 0
                else:
                    floor_match = re.search(r'\d+', floor_text)
                    if floor_match:
                        floor = int(floor_match.group(0))


        image_element = offer_card.find("img", attrs={"data-cy": "gallerySliderImgThumbnail"})
        if image_element:
            image_url = image_element.get("src")

        results.append({
            "city": SELECTED_CITY,
            "street": street,
            "price_pln": price,
            "size_m2": size,
            "rooms": rooms,
            "floor": floor,
            "detail_url": detail_url,
            "image_url": image_url
            }
        )

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
                coords_data = data.get("coords", None)
                if coords_data:
                    coords_data = coords_data.split(',')
                    longitude = coords_data[1]
                    latitude = coords_data[0]
                    return {
                        "longitude": longitude,
                        "latitude": latitude
                    }

                return None
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
            item["intercom"] = parse_boolean_feature(detail_soup, DOMOFON_RE)
            item["basement"] = parse_boolean_feature(detail_soup, PIWNICA_RE)
            item["furnished"] = parse_boolean_feature(detail_soup, MEBLE_RE)
            item["elevator"] = parse_boolean_feature(detail_soup, WINDA_RE)
            item["parking_space"] = parse_boolean_feature(detail_soup, MIEJSCE_POSTOJOWE_RE)
            item["gated_property"] = parse_boolean_feature(detail_soup, OBIEKT_ZAMKNIETY_RE)
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

            item["longitude"] = coords["longitude"] if coords else None
            item["latitude"] = coords["latitude"] if coords else None


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
    
    data = asyncio.run(scrape_prices_and_streets(BASE_URL, pages=20))

    output_file = os.path.join(output_dir, f"{SELECTED_CITY}-morizon.csv")
    df = pd.DataFrame(data)
    df.to_csv(output_file, index=False, encoding="utf-8-sig")
    print(f"Data saved to {output_file}")

