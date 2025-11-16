import json
import os.path
from contextlib import asynccontextmanager
from enum import Enum
import pandas as pd

import joblib
from fastapi import FastAPI, HTTPException, Request
from fastapi.exceptions import RequestValidationError
from pydantic import create_model
from typing import Optional
from dataPreparator import prepareDataAfterLoad
from fastapi.responses import JSONResponse
import logging

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

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)
app = FastAPI(lifespan=lifespan)

@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request: Request, exc: RequestValidationError):
    error_details = exc.errors()
    logger.error(f"Validation error for request to {request.url}: {error_details}")
    return JSONResponse(
        status_code=422,
        content={"detail": error_details},
    )

@app.post("/predict/{model}")
async def predict(model: ModelName, request: DynamicPredictionInput):
    print("incoming request")
    print(request)

    if model.value not in MODELS:
        raise HTTPException(status_code=404, detail=f"{model.value} isn't loaded")


    try:
        data = request.dict(exclude_none=True)
        print(data)
        if not isinstance(data, dict):
            raise HTTPException(status_code=400, detail="Request should be valid dictionary")
    except json.JSONDecodeError:
        raise HTTPException(status_code=400, detail="Invalid JSON format")
    except Exception as ex:
        print(f"Exception when getting data: {ex}")
        raise HTTPException(status_code=500, detail=ex)

    try:
        input_dataframe = pd.DataFrame([data])

        final_dataframe = prepareDataAfterLoad(input_dataframe)
        model_pipeline = MODELS[model.value]
        result = model_pipeline.predict(final_dataframe)

        return {
            "predictedPricePln": round(float(result[0]), 2)
        }
    except Exception as ex:
        print(f"error when predicting price: {ex}")
        raise HTTPException(status_code=500, detail=f"Error when predicting price: {ex}")
