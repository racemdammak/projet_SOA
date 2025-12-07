from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from main import summarize_pdf
from operation_logger import OperationLogger
import os
from pathlib import Path
import traceback

app = FastAPI()

origins = [
    "http://localhost:3000",  # React dev server
    "http://127.0.0.1:3000",  # React dev server (alternative)
]

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

class SummarizeRequest(BaseModel):
    filename: str

def find_storage_directory():
    current = Path(__file__).parent.absolute()
    cloudws_path = current.parent / "CloudWS" / "MiniCloudStorage"
    return str(cloudws_path)

STORAGE_DIR = find_storage_directory()

@app.get("/")
async def read_root():
    return {
        "Racem": "Dammak",
        "dossier cloud": STORAGE_DIR,
        "dossier existe": os.path.exists(STORAGE_DIR)
    }

@app.post("/summarize")
async def summarize_pdf_endpoint(request: SummarizeRequest):
    pdf_path = os.path.join(STORAGE_DIR, request.filename)
    
    if not os.path.isfile(pdf_path):
        raise HTTPException(
            status_code=400,
            detail=f"Ce fichier n'est pas un text: {request.filename}"
        )
    
    summary = summarize_pdf(pdf_path)
    
    file_size = os.path.getsize(pdf_path) if os.path.exists(pdf_path) else None
    OperationLogger.log_operation(
        OperationLogger.OperationType.SUMMARIZE,
        request.filename,
        OperationLogger.OperationStatus.SUCCESS,
        "Résumé généré avec succès.",
        file_size
    )
    
    return {"summary": summary}