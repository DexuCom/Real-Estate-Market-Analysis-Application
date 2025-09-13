from joblib import dump

from dataPreparator import prepareData
from sklearn.svm import SVR
from sklearn.model_selection import KFold, cross_validate, GridSearchCV, RandomizedSearchCV
from sklearn.preprocessing import StandardScaler
from sklearn.pipeline import Pipeline
import numpy as np

K = 10
RANDOM_SEED = 621379

SVR_SEARCH_CONFIG = {
    'svr__C': np.logspace(8, 8, 10),
    'svr__gamma': np.logspace(-3, 2, 6),
    'svr__epsilon': [0.001, 0.01, 0.1, 1],
    'svr__kernel': ['rbf']
}

FINISHED_MODEL_PATH = "models/svr_rema_model.joblib"
TARGET_COLUMN = 'price_pln'

dataframe = prepareData()

y = dataframe[TARGET_COLUMN]
X = dataframe.drop(TARGET_COLUMN, axis='columns')

pipeline = Pipeline([
    ('scaler', StandardScaler()),
    ('svr', SVR(epsilon=0.1))
])

kfold = KFold(n_splits=K, shuffle=True, random_state=RANDOM_SEED)

print("\nRunning search")
search = RandomizedSearchCV(
    pipeline,
    SVR_SEARCH_CONFIG,
    cv=kfold,
    scoring='r2',
    verbose=2,
    n_jobs=-1,
    random_state= RANDOM_SEED,
    n_iter=100
)

search.fit(X, y)

print("\n------ Grid search results --------")
print(f"Best params: {search.best_params_}")
print(f"Best r^2: {search.best_score_:.3f}")
print("--------------------------------------")

best_model_pipeline = search.best_estimator_

scoring_metrics = ['r2', 'neg_mean_absolute_error', 'neg_mean_squared_error']

final_scores = cross_validate(best_model_pipeline, X, y, cv=kfold, scoring=scoring_metrics)

mean_r2 = np.mean(final_scores['test_r2'])
std_r2 = np.std(final_scores['test_r2'])

mean_mae = -np.mean(final_scores['test_neg_mean_absolute_error'])
std_mae = np.std(final_scores['test_neg_mean_absolute_error'])

mean_rmse = np.sqrt(-np.mean(final_scores['test_neg_mean_squared_error']))
std_rmse = np.std(np.sqrt(-final_scores['test_neg_mean_squared_error']))

print("\n--- Final model scores  ---")
print(f"Average R^2: {mean_r2:.3f} (± {std_r2:.3f})")
print(f"Average MAE: {mean_mae:,.2f} PLN (± {std_mae:,.2f} PLN)")
print(f"Average RMSE: {mean_rmse:,.2f} PLN (± {std_rmse:,.2f} PLN)")
print("--------------------------------------------------")

dump(best_model_pipeline, FINISHED_MODEL_PATH)
print(f"Saved to {FINISHED_MODEL_PATH}")


