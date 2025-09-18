import json
import os.path
from contextlib import asynccontextmanager
from enum import Enum
import pandas as pd

import joblib
from fastapi import FastAPI, HTTPException
from pydantic import create_model
from typing import Optional

MODELS = {}
MODEL_PATHS = {
    "svr": "models/svr_rema_model.joblib",
    "rfr": "models/rfr_rema_model.joblib",
    "xgb": "models/xgb_rema_model.joblib"
}

FEATURES_INFO = {}
try:
    with open("models/features.json", "r") as f:
        FEATURES_INFO = json.load(f)
except Exception as ex:
    print(f"Error loading features: {ex}")

class ModelName(str, Enum):
    svr = "svr"
    rfr = "rfr"
    xgb = "xgb"

STR_TO_TYPE = {"int": int, "float": float, "str": str, "bool": bool}
FEATURES_WITH_TYPES = {
    name: (Optional[STR_TO_TYPE[feature_type]], None)
    for name, feature_type in FEATURES_INFO.items()
}



DynamicPredictionInput = create_model(
    "DynamicPredictionInput",
    **FEATURES_WITH_TYPES
)


@asynccontextmanager
async def lifespan(app: FastAPI):
    print("Loading models...")
    for model in MODEL_PATHS:
        path = MODEL_PATHS[model]

        if os.path.exists(path):
            try:
                MODELS[model] = joblib.load(path)
                print(f"Loaded {model}")
            except Exception as ex:
                print(f"Error loading model {model}: {ex}")
        else:
            print(f"{model} does not exist in models, train it.")

    print("Finished loading models")
    yield
    print("Cleanup")

app = FastAPI(lifespan=lifespan)

@app.post("/predict/{model}")
async def predict(model: ModelName, request: DynamicPredictionInput):
    if model.value not in MODELS:
        raise HTTPException(status_code=404, detail=f"{model.value} isn't loaded")


    try:
        data = request.dict(exclude_none=True)
        print(data)
        if not isinstance(data, dict):
            raise HTTPException(status_code=400, detail="Request should be valid dictionary")
    except json.JSONDecodeError:
        raise HTTPException(status_code=400, detail="Invalid JSON format")

    try:
        input_dataframe = pd.DataFrame([data])
        input_dataframe_encoded = pd.get_dummies(input_dataframe)
        final_dataframe = input_dataframe_encoded.reindex(columns=FEATURES_WITH_TYPES.keys(), fill_value=0)

        model_pipeline = MODELS[model.value]
        result = model_pipeline.predict(final_dataframe)

        return {
            "predictedPricePln": round(float(result[0]), 2)
        }
    except Exception as ex:
        raise HTTPException(status_code=500, detail=f"Error when predicting price: {ex}")
