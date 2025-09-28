import matplotlib.pyplot as plt
import seaborn

from dataPreparator import prepareData

try:
    dataframe_with_encoding = prepareData()

    print("\n\nFull head after encoding: ")
    print(dataframe_with_encoding.head())

    correlation_matrix = dataframe_with_encoding.corr()

    corr_with_price = correlation_matrix['price_pln']
    corr_with_price = corr_with_price.drop('price_pln')
    corr_with_price_sorted = corr_with_price.sort_values(ascending=False)
    plt.figure(figsize=(12, 10))
    seaborn.barplot(x=corr_with_price_sorted.values, y=corr_with_price_sorted.index, palette='coolwarm_r')

    plt.title('Korelacja  cech z price_pln', fontsize=16)
    plt.xlabel('Korelacja', fontsize=12)
    plt.ylabel('Cecha', fontsize=12)
    plt.grid(axis='x', linestyle='--', alpha=0.6)
    plt.tight_layout()
    plt.show()


    # plt.figure(figsize=(20, 16))
    # seaborn.heatmap(correlation_matrix, annot=True, cmap='coolwarm', fmt=".2f", linewidths=0.5)
    # plt.title("Macierz korelacji")
    # plt.tight_layout()
    # plt.show()



except Exception as ex:
    print(ex)