import os

import numpy as np
import pandas
FILE_PATH = "../Scraper/ScraperOutput/Gdańsk-morizon.csv"

def prepareData():
    script_dir = os.path.dirname(os.path.abspath(__file__))
    relative_file_path = os.path.join(script_dir, FILE_PATH)
    dataframe = pandas.read_csv(relative_file_path)

    # TODO discuss with the team
    columns_to_drop = ['city', 'street', 'detail_url', 'image_url']
    dataframe_cleaned = dataframe.drop(columns=columns_to_drop)
    dataframe_cleaned['heating'] = dataframe_cleaned['heating'].replace('-1', 'Unknown')

    num_columns_with_unknown = ['year_built', 'total_floors']

    for col in num_columns_with_unknown:
        if col in dataframe_cleaned.columns:
            indicator_col_name = f"{col}_is_unknown"
            dataframe_cleaned[indicator_col_name] = (dataframe_cleaned[col] == -1).astype(int)

            median_value = dataframe_cleaned[dataframe_cleaned[col] != -1][col].median()
            dataframe_cleaned[col] = dataframe_cleaned[col].replace(-1, median_value)

    num_columns = dataframe_cleaned.select_dtypes(include=np.number).columns.tolist()
    for col in num_columns:
        print(col)
        median = dataframe_cleaned[col].median()
        dataframe_cleaned.fillna({col: median}, inplace=True)

    dataframe_with_encoding = dataframe_cleaned.copy()
    category_columns = dataframe_with_encoding.select_dtypes(include=['object']).columns

    dataframe_with_encoding = pandas.get_dummies(dataframe_with_encoding, columns=category_columns, drop_first=False)

    return dataframe_with_encoding