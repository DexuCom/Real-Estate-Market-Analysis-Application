from xgboost import plot_tree
import matplotlib.pyplot as plt
from joblib import load

FINISHED_MODEL_PATH = "models/xgb_rema_model.joblib"
xgb_model = load(FINISHED_MODEL_PATH).named_steps['xgb']

plt.figure(figsize=(20,10))
plot_tree(xgb_model, num_trees=0)
plt.show()
