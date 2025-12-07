import google.generativeai as genai
from dotenv import load_dotenv
import os
load_dotenv()

api_key = os.getenv("GEMINI_API_KEY")
genai.configure(api_key=api_key)

configuration = {
    "temperature": 1,
    "top_p": 0.95,
    "top_k": 40,
    "max_output_tokens": 8192,
    "response_mime_type": "text/plain"
}
model = genai.GenerativeModel(
    model_name="gemini-2.5-flash",
    generation_config=configuration
)

def generate_text(text):
    prompt=f"""
    Tu es un assistant spécialisé dans la synthèse d’informations. Résume clairement et fidèlement le texte suivant, 
    en gardant uniquement les idées essentielles, sans ajouter d'informations externes. Utilise un style concis, cohérent et facile à lire. 
    Si le texte contient plusieurs parties, organise le résumé sous forme de points clés.

    Texte à résumer :
    {text}

    """
    response = model.generate_content(prompt)
    
    return response.text

'''for m in genai.list_models():
    print(m.name)
'''