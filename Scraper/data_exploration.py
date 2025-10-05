import time
import osmium
from scipy.spatial import cKDTree
from geopy.distance import geodesic
import numpy as np

OSM_FILE_PATH = "GIS_data/poland_amenities.osm.pbf"

DESIRED_AMENITIES = {"police", "place_of_worship", "kindergarten", "clothes", "parking", "restaurant", "townhall",
                     "grocery", "school", "shop", "pharmacy", "prison"}
kdtree_by_type = {}

# useful reference
# https://docs.osmcode.org/pyosmium/latest/user_manual/05-Working-with-Handlers/
class OsmHandler:
    def __init__(self):
        self.amenities_by_type = {amenity: [] for amenity in DESIRED_AMENITIES}
        self.amenity_types = set()
    def node(self, n):
        if 'amenity' in n.tags:
            self.amenity_types.add(n.tags['amenity'])
        if "amenity" in n.tags and n.tags['amenity'] in DESIRED_AMENITIES:
            self.amenities_by_type[n.tags['amenity']].append((n.location.lat, n.location.lon))


def init():
    start_time = time.perf_counter()

    osm_handler = OsmHandler()
    osmium.apply(OSM_FILE_PATH, osm_handler)

    print(osm_handler.amenity_types)
    end_time = time.perf_counter()
    total_time = end_time - start_time
    print(f"\nLoading finished in {total_time} seconds\n")

    print("Creating k-d trees")
    start_time = time.perf_counter()

    for amenity_type, coords_list in osm_handler.amenities_by_type.items():
        coords_array = np.array(coords_list)
        kdtree_by_type[amenity_type] = cKDTree(coords_array)

    end_time = time.perf_counter()
    total_time = end_time - start_time
    print(f"\nK-D trees finished in {total_time} seconds\n")

    print("INIT COMPLETED")

def find_nearest_amenity(lat, lon, type):
    point = (lat, lon)
    kdtree = kdtree_by_type[type]

    distance, idx = kdtree.query(point, k=1)
    nearest_coords = tuple(kdtree.data[idx])

    distance_km = geodesic(point, nearest_coords).kilometers
    return nearest_coords, distance_km


if __name__ == "__main__":
    init()

    coords, dist = find_nearest_amenity(54.399324, 18.581347, "school")
    print(coords)
    print(dist)