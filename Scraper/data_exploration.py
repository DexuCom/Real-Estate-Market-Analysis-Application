import time
import osmium
from scipy.spatial import cKDTree
from geopy.distance import geodesic
import numpy as np
from shapely.geometry import Polygon
import pickle
import os
import pandas as pd

OSM_FILE_PATH = "GIS_data/poland_amenities.osm.pbf"

# DESIRED_AMENITIES = {"police", "place_of_worship", "kindergarten", "clothes", "parking",
#                      "restaurant", "townhall", "grocery", "school", "shop", "pharmacy", "prison"}

DESIRED_AMENITIES = {'pharmacy', 'parking', 'restaurant', 'school', 'police', 'shop', 'kindergarten', 'clothes', 'grocery', 'place_of_worship', 'townhall', 'prison'}

CACHE_FILE = "amenities_cache.pkl"

INPUT_CSV = "ScraperOutput/Gdańsk-morizon.csv"
OUTPUT_CSV = "ScraperOutput/oferty.csv"

kdtree_by_type = {}
def save_cache(handler):
    with open(CACHE_FILE, "wb") as f:
        pickle.dump(handler.amenities_by_type, f)

def load_cache():
    if os.path.exists(CACHE_FILE):
        with open(CACHE_FILE, "rb") as f:
            return pickle.load(f)
    return None

class OsmHandler(osmium.SimpleHandler):
    def __init__(self):
        super().__init__()
        self.amenities_by_type = {amenity: [] for amenity in DESIRED_AMENITIES}
        self.amenity_types = set()
        self.nodes = {}  # node_id -> (lat, lon)

    def node(self, n):
        if n.location.valid():
            self.nodes[n.id] = (n.location.lat, n.location.lon)
        if "amenity" in n.tags:
            self.amenity_types.add(n.tags['amenity'])
            if n.tags['amenity'] in DESIRED_AMENITIES:
                self.amenities_by_type[n.tags['amenity']].append((n.location.lat, n.location.lon))

    def way(self, w):
        if "amenity" in w.tags and w.tags['amenity'] in DESIRED_AMENITIES:
            coords = []
            for nref in w.nodes:
                if nref.ref in self.nodes:
                    coords.append(self.nodes[nref.ref])

            # tzw closed way, musi być obszar zamknięty
            if len(coords) >= 3 and coords[0] == coords[-1]:
                poly = Polygon([(lon, lat) for lat, lon in coords])
                centroid = poly.centroid
                self.amenities_by_type[w.tags['amenity']].append((centroid.y, centroid.x))

def init():
    global kdtree_by_type
    cached_data = load_cache()
    if cached_data:
        print("Loading amenities from cache...")
        amenities_by_type = cached_data
    else:
        print("Processing OSM file...")
        start_time = time.perf_counter()
        osm_handler = OsmHandler()
        osmium.apply(OSM_FILE_PATH, osm_handler)
        amenities_by_type = osm_handler.amenities_by_type
        save_cache(osm_handler)
        print(f"Processing finished in {time.perf_counter() - start_time:.2f} seconds")

    print("Creating k-d trees...")
    amenity_types = set()
    for amenity_type, coords_list in amenities_by_type.items():
        amenity_types.add(amenity_type)
        if coords_list:
            coords_array = np.array(coords_list)
            kdtree_by_type[amenity_type] = cKDTree(coords_array)
    print("KD-Trees ready")

    print(amenity_types)

def find_nearest_amenity(lat, lon, type):
    if type not in kdtree_by_type:
        return None, None
    point = (lat, lon)
    kdtree = kdtree_by_type[type]

    distance, idx = kdtree.query(point, k=1)
    nearest_coords = tuple(kdtree.data[idx])
    distance_km = geodesic(point, nearest_coords).kilometers
    return nearest_coords, distance_km

if __name__ == "__main__":
    init()
    df = pd.read_csv(INPUT_CSV)
    # for amenity_type in DESIRED_AMENITIES:
    #     df[f"distance_to_{amenity_type}"] = -1
    #
    # for idx, row in df.iterrows():
    #     lat = row['latitude']
    #     lon = row['longitude']
    #     for amenity_type in DESIRED_AMENITIES:
    #         coords, dist_km = find_nearest_amenity(lat, lon, amenity_type)
    #         df.at[idx, f"distance_to_{amenity_type}"] = dist_km

    df.to_csv(OUTPUT_CSV, index=False)
    print(f"Saved to {OUTPUT_CSV}")

