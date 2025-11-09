from xgboost import plot_tree
import shap
import matplotlib.pyplot as plt
from joblib import load
from dataPreparator import prepareData

def showDependencyValuesForFeature(feature_name):
    global X
    global shap_values

    shap.dependence_plot(
        feature_name,
        shap_values,
        X,
        interaction_index=None,
        show=False
    )
    plt.title(f"Wpływ cechy {feature_name} na przewidywaną cenę nieruchomości", fontsize=14)
    plt.xlabel(f"{feature_name}", fontsize=12)
    plt.ylabel("Wartość SHAP", fontsize=12)
    plt.tight_layout()
    plt.show()


FINISHED_MODEL_PATH = "models/xgb_rema_model.joblib"
xgb_model = load(FINISHED_MODEL_PATH).named_steps['xgb']

offer_data = prepareData()
X = offer_data.drop("price_pln", axis='columns')


tree_explainer = shap.TreeExplainer(xgb_model)
shap_values = tree_explainer.shap_values(X)

## completely unreadable, ignore
# plt.figure()
# shap.summary_plot(shap_values, X, show=False)
# plt.title("Ważność cech przy predykcji ceny nieruchomości")
# plt.tight_layout()
# plt.show()


# plt.figure()
# shap.summary_plot(
#     shap_values,
#     X,
#     plot_type="bar",
#     show=False,
#     max_display=15
# )
# plt.xlabel("Średnia bezwzględna wartość wpływu cechy (|SHAP|)", fontsize=12)
# plt.title("Średni wpływ cech na predykcję ceny nieruchomości")
# plt.tight_layout()
# plt.show()


showDependencyValuesForFeature("size_m2")
showDependencyValuesForFeature("latitude")
showDependencyValuesForFeature("longitude")
showDependencyValuesForFeature("year_built")