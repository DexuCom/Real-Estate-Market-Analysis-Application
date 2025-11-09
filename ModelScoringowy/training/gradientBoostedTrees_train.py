from sklearn.preprocessing import StandardScaler
from sklearn.pipeline import Pipeline
import xgboost as xgb
from trainHelper import trainModel

K = 10
RANDOM_SEED = 621379

FINISHED_MODEL_PATH = "../models/xgb_rema_model.joblib"
TARGET_COLUMN = 'price_pln'

XGBOOST_SEARCH_CONFIG = {
    'xgb__n_estimators': [100, 500, 1000, 1500],
    'xgb__learning_rate': [0.01, 0.05, 0.1, 0.2],
    'xgb__max_depth': [3, 5, 7, 9],
    'xgb__subsample': [0.6, 0.7, 0.8, 0.9, 1.0],
    'xgb__colsample_bytree': [0.6, 0.7, 0.8, 0.9, 1.0],
    'xgb__min_child_weight': [1, 5, 10],

}


pipeline = Pipeline([
    ('scaler', StandardScaler().set_output()),
    ('xgb', xgb.XGBRegressor(random_state=RANDOM_SEED))
])

trainModel(pipeline, XGBOOST_SEARCH_CONFIG, FINISHED_MODEL_PATH, K, RANDOM_SEED)