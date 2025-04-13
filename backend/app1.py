from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import pickle
import bz2
import numpy as np
from sklearn.decomposition import PCA
from sklearn.preprocessing import StandardScaler

# Load Standard Scaler
scalar_object = bz2.BZ2File("Model/standardScalar.pkl", "rb")
scaler: StandardScaler = pickle.load(scalar_object)

# Load PCA Model
pca_object = bz2.BZ2File("Model/pcaModel.pkl", "rb")
pca: PCA = pickle.load(pca_object)

# Load ELM Model
model_for_pred = bz2.BZ2File("Model/modelForPrediction.pkl", "rb")
model = pickle.load(model_for_pred)

# FastAPI app
app = FastAPI(title="Diabetes Prediction API")

# Input Schema
class PatientData(BaseModel):
    pregnancies: float
    glucose: float
    blood_pressure: float
    skin_thickness: float
    insulin: float
    bmi: float
    diabetes_pedigree_function: float
    age: float

@app.post("/predict")
def predict(data: PatientData):
    try:
        # Convert input to numpy array
        input_array = np.array([
            data.pregnancies,
            data.glucose,
            data.blood_pressure,
            data.skin_thickness,
            data.insulin,
            data.bmi,
            data.diabetes_pedigree_function,
            data.age
        ]).reshape(1, -1)

        # Preprocess: Scale and apply PCA
        scaled_data = scaler.transform(input_array)
        transformed_data = pca.transform(scaled_data)

        # Make prediction
        prediction = model.predict(transformed_data)

        result = {
            "prediction": "Diabetic" if prediction[0] == 1 else "Non-Diabetic",
            "status_code": 200
        }

        return result
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Prediction failed: {str(e)}")
