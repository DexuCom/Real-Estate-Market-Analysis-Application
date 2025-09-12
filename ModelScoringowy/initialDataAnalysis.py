import pandas
import matplotlib.pyplot as plt
import seaborn

FILE_PATH = "../ScraperOutput/Gdańsk-morizon.csv"

try:
    dataframe = pandas.read_csv(FILE_PATH)
    dataframeWithDummies = pandas.get_dummies(dataframe)
    correlationMatrix = dataframeWithDummies.corr()

    plt.figure()
    seaborn.heatmap(correlationMatrix, annot=True, cmap='coolwarm', fmt=".2f", linewidths=0.5)
    plt.title("Macierz korelacji")
except Exception as ex:
    print(ex)