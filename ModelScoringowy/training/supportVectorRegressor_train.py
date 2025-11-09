from sklearn.svm import SVR
from sklearn.preprocessing import StandardScaler
from sklearn.pipeline import Pipeline
import numpy as np
from trainHelper import trainModel

K = 10
RANDOM_SEED = 621379

SVR_SEARCH_CONFIG = {

    # 'svr__C': np.logspace(7, 7, 10),
    # 'svr__gamma': np.logspace(-3, 2, 6),
    # 'svr__epsilon': [0.001, 0.01, 0.1, 1],

    # Current best
    'svr__C': [np.float64(1000000.0)],
    'svr__gamma': [np.float64(0.01)],
    'svr__epsilon': [0.1],
    'svr__kernel': ['rbf']
}

FINISHED_MODEL_PATH = "../models/svr_rema_model.joblib"

pipeline = Pipeline([
        ('scaler', StandardScaler()),
        ('svr', SVR(epsilon=0.1, cache_size=4000, tol=1e-3))
    ])

trainModel(pipeline, SVR_SEARCH_CONFIG, FINISHED_MODEL_PATH, K, RANDOM_SEED)

