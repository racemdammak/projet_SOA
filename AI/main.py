import fitz
import google.generativeai as genai
from dotenv import load_dotenv
from genai import generate_text

def extract_text_from_pdf(pdf_path):
    """Extract text from a PDF file."""
    doc = fitz.open(pdf_path)
    text = ""
    for page in doc:
        text += page.get_text()
    return text

def summarize_pdf(pdf_path):
    """Extract text from a PDF and generate a summary."""
    text = extract_text_from_pdf(pdf_path)
    summary = generate_text(text)
    return summary
