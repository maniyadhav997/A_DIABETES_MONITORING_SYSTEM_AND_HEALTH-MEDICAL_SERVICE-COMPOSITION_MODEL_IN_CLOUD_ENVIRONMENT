from fastapi import FastAPI, Request, Form
from fastapi.responses import HTMLResponse
from fastapi.templating import Jinja2Templates
from fastapi.staticfiles import StaticFiles
import pickle
import bz2
import numpy as np
from pathlib import Path
from fastapi.middleware.cors import CORSMiddleware

app = FastAPI()

# CORS middleware setup (if needed)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Setup for templates and static files
BASE_DIR = Path(__file__).resolve().parent
templates = Jinja2Templates(directory=str(BASE_DIR / "templates"))
app.mount("/static", StaticFiles(directory=str(BASE_DIR / "static")), name="static")

# Load models
scaler = pickle.load(bz2.BZ2File("Model/standardScalar.pkl", "rb"))
pca = pickle.load(bz2.BZ2File("Model/pcaModel.pkl", "rb"))
model = pickle.load(bz2.BZ2File("Model/modelForPrediction.pkl", "rb"))

# Home Page
@app.get("/", response_class=HTMLResponse)
async def index(request: Request):
    return templates.TemplateResponse("index.html", {"request": request})


# Prediction Endpoint (Form Handling)
@app.post("/predictdata", response_class=HTMLResponse)
async def predict_data(
    request: Request,
    Pregnancies: float = Form(...),
    Glucose: float = Form(...),
    BloodPressure: float = Form(...),
    SkinThickness: float = Form(...),
    Insulin: float = Form(...),
    BMI: float = Form(...),
    DiabetesPedigreeFunction: float = Form(...),
    Age: float = Form(...)
):
    try:
        # Format input
        input_data = np.array([[Pregnancies, Glucose, BloodPressure, SkinThickness,
                                Insulin, BMI, DiabetesPedigreeFunction, Age]])

        # Preprocess input
        scaled_data = scaler.transform(input_data)
        transformed_data = pca.transform(scaled_data)

        # Make prediction
        prediction = model.predict(transformed_data)
        result = "Diabetic" if prediction[0] == 1 else "Non-Diabetic"

        return templates.TemplateResponse("single_prediction.html", {
            "request": request,
            "result": result
        })
    except Exception as e:
        return templates.TemplateResponse("home.html", {
            "request": request,
            "result": f"Error: {str(e)}"
        })
