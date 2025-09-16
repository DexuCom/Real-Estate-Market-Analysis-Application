
from sklearn.ensemble import RandomForestRegressor
from sklearn.preprocessing import StandardScaler
from sklearn.pipeline import Pipeline
import numpy as np
from trainHelper import trainModel

K = 10
RANDOM_SEED = 621379

FINISHED_MODEL_PATH = "../models/rfr_rema_model.joblib"

RANDOM_FOREST_SEARCH_CONFIG = {
    'rf__n_estimators': [int(x) for x in np.linspace(start=100, stop=1200, num=12)],
    'rf__max_features': ['auto', 'sqrt'],
    'rf__max_depth': [5, 10, 20, None],
    'rf__min_samples_split': [2, 5, 10],
    'rf__min_samples_leaf': [1, 2, 4],
    'rf__bootstrap': [True, False]
}

pipeline = Pipeline([
    ('scaler', StandardScaler()),
    ('rf', RandomForestRegressor(random_state=RANDOM_SEED))
])

trainModel(pipeline, RANDOM_FOREST_SEARCH_CONFIG, FINISHED_MODEL_PATH, K, RANDOM_SEED)

