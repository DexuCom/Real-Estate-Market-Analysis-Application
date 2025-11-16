import os

import numpy as np
import pandas
# FILE_PATH = "../Scraper/ScraperOutput/Gdańsk-morizon.csv"
FILE_PATH = "../Scraper/ScraperOutput/oferty.csv"

# NORMAL HEATING
HEATING_RANKING = {
    "Inne": 3,
    "C.O. miejskie": 6,
    "C.O. gazowe": 4,
    "C.O. elektryczne": 1,
    "C.O. własne": 3,
    "Kotłownia": 2,
    "Pompa ciepła": 5
}

# HEATING_RANKING = {
#     "Inne": 5,
#     "C.O. miejskie": 10,
#     "C.O. gazowe": 4,
#     "C.O. elektryczne": 3,
#     "C.O. własne": 5,
#     "Kotłownia": 4,
#     "Pompa ciepła": 9
# }


def prepareData():
    script_dir = os.path.dirname(os.path.abspath(__file__))
    relative_file_path = os.path.join(script_dir, FILE_PATH)
    dataframe = pandas.read_csv(relative_file_path)
    columns_to_drop = ['city', 'street', 'detail_url', 'image_url']
    dataframe_cleaned = dataframe.drop(columns=columns_to_drop)
    prepareDataAfterLoad(dataframe_cleaned)

def prepareDataAfterLoad(dataframe):
    # TODO discuss with the team


    dataframe['heating'] = dataframe['heating'].replace('-1', 'Inne')
    dataframe['heating_is_unknown'] = (dataframe['heating'] == 'Inne').astype(int)
    dataframe['heating'] = dataframe['heating'].map(HEATING_RANKING)

    dataframe['market'] = (dataframe['market'] == 'pierwotny').astype(int)
    num_columns_with_unknown = ['year_built', 'total_floors']

    for col in num_columns_with_unknown:
        if col in dataframe.columns:
            indicator_col_name = f"{col}_is_unknown"
            dataframe[indicator_col_name] = (dataframe[col] == -1).astype(int)

            median_value = dataframe[dataframe[col] != -1][col].median()
            dataframe[col] = dataframe[col].replace(-1, median_value)

    num_columns = dataframe.select_dtypes(include=np.number).columns.tolist()
    for col in num_columns:
        median = dataframe[col].median()
        dataframe.fillna({col: median}, inplace=True)

    dataframe_with_encoding = dataframe.copy()
    category_columns = dataframe_with_encoding.select_dtypes(include=['object']).columns

    dataframe_with_encoding = pandas.get_dummies(dataframe_with_encoding, columns=category_columns, drop_first=False)
    print(dataframe_with_encoding.columns.tolist())
    print(dataframe_with_encoding.head())
    return dataframe_with_encoding