# Projet SOA - MiniCloud

Un système de stockage cloud complet avec services SOAP/REST, interface React et intégration d'IA pour la résumé de documents PDF.

## 📋 Table des matières

- [Description](#description)
- [Architecture](#architecture)
- [Prérequis](#prérequis)
- [Installation](#installation)
- [Configuration](#configuration)
- [Démarrage](#démarrage)
- [Utilisation](#utilisation)
- [Structure du projet](#structure-du-projet)
- [API Endpoints](#api-endpoints)
- [Dépannage](#dépannage)

## 🎯 Description

Ce projet est une application de stockage cloud multi-services qui permet :
- **Upload, téléchargement et suppression de fichiers** via des services SOAP et REST
- **Interface web moderne** en React pour gérer les fichiers
- **Résumé automatique de PDF** grâce à l'intégration de Google Gemini AI
- **Journalisation des opérations** dans un fichier XML

## 🏗️ Architecture

Le projet est composé de trois modules principaux :

1. **CloudWS** (Backend Java) : Services SOAP et REST pour la gestion des fichiers
2. **mini-cloud-react** (Frontend React) : Interface utilisateur web
3. **AI** (Service Python) : API FastAPI pour le résumé de documents PDF avec IA

```
┌─────────────────┐
│  React Frontend │ (Port 3000)
│  (mini-cloud)   │
└────────┬────────┘
         │
         ├─────────────────┐
         │                 │
         ▼                 ▼
┌─────────────────┐  ┌──────────────┐
│  REST Server    │  │  AI Service  │
│  (Port 4567)    │  │  (Port 8000) │
└────────┬────────┘  └──────────────┘
         │
         ▼
┌─────────────────┐
│  CloudStorage   │
│  (MiniCloudStorage)│
└─────────────────┘

┌─────────────────┐
│  SOAP Server    │ (Port 8089)
│  (Standalone)   │
└─────────────────┘
```

## 📦 Prérequis

### Pour le backend Java (CloudWS)
- **Java JDK 8** ou supérieur
- **Maven 3.6+** pour la gestion des dépendances

### Pour le frontend React (mini-cloud-react)
- **Node.js 14+** et **npm** (ou **yarn**)

### Pour le service AI (AI)
- **Python 3.8+**
- **pip** (gestionnaire de paquets Python)

### Autres
- **Clé API Google Gemini** pour le service de résumé IA
  - Obtenez votre clé sur : https://makersuite.google.com/app/apikey

## 🔧 Installation

### 1. Cloner le projet

```bash
git clone <url-du-repo>
cd projet_soa
```

### 2. Installation du backend Java

```bash
cd CloudWS
mvn clean install
```

Cette commande va :
- Télécharger toutes les dépendances Maven
- Compiler le projet Java
- Générer le fichier JAR dans `target/MiniCloud_SOAP_REST-1.0-SNAPSHOT.jar`

### 3. Installation du frontend React

```bash
cd mini-cloud-react
npm install
```

Cette commande va installer toutes les dépendances npm nécessaires.

### 4. Installation du service AI Python

```bash
cd AI
pip install -r requirements.txt
```

Les dépendances installées sont :
- `dotenv` : Gestion des variables d'environnement
- `google-generativeai` : SDK Google Gemini AI
- `pymupdf` (PyMuPDF) : Extraction de texte depuis les PDF
- `fastapi` : Framework web pour l'API
- `uvicorn` : Serveur ASGI pour FastAPI

## ⚙️ Configuration

### Configuration du service AI

1. Créez un fichier `.env` dans le dossier `AI/` :

```bash
cd AI
touch .env
```

2. Ajoutez votre clé API Google Gemini dans le fichier `.env` :

```env
GEMINI_API_KEY=votre_cle_api_ici
```

**Important** : Remplacez `votre_cle_api_ici` par votre vraie clé API obtenue depuis Google.

### Vérification de la structure des dossiers

Assurez-vous que le dossier de stockage existe :
- Le dossier `CloudWS/MiniCloudStorage/` sera créé automatiquement lors du premier démarrage
- Ce dossier contiendra tous les fichiers uploadés

## 🚀 Démarrage

Le projet nécessite de démarrer **trois services** en parallèle. Ouvrez **trois terminaux** différents.

### Terminal 1 : Service REST (Java)

```bash
cd CloudWS
java -cp "target/classes:target/MiniCloud_SOAP_REST-1.0-SNAPSHOT.jar:$(mvn dependency:build-classpath -Dmdep.outputFile=/dev/stdout -q)" cloud.rest.RestServer
```

**Alternative avec Maven** (si les dépendances sont dans le classpath) :

```bash
cd CloudWS
mvn exec:java -Dexec.mainClass="cloud.rest.RestServer"
```

Le serveur REST démarre sur **http://localhost:4567**

### Terminal 2 : Service SOAP (Java) - Optionnel

```bash
cd CloudWS
java -cp "target/classes:target/MiniCloud_SOAP_REST-1.0-SNAPSHOT.jar:$(mvn dependency:build-classpath -Dmdep.outputFile=/dev/stdout -q)" cloud.soap.server.SoapServer
```

**Alternative avec Maven** :

```bash
cd CloudWS
mvn exec:java -Dexec.mainClass="cloud.soap.server.SoapServer"
```

Le serveur SOAP démarre sur **http://localhost:8089/cloud?wsdl**

### Terminal 3 : Service AI (Python)

```bash
cd AI
python -m uvicorn server:app --reload --port 8000
```

Le service AI démarre sur **http://localhost:8000**

### Terminal 4 : Frontend React

```bash
cd mini-cloud-react
npm start
```

Le frontend démarre sur **http://localhost:3000** et s'ouvre automatiquement dans votre navigateur.

## 📱 Utilisation

### Interface Web

1. Ouvrez votre navigateur à l'adresse **http://localhost:3000**
2. **Upload de fichiers** :
   - Glissez-déposez des fichiers dans la zone prévue
   - Ou cliquez pour sélectionner des fichiers
3. **Télécharger un fichier** : Cliquez sur le bouton "Télécharger" d'un fichier
4. **Supprimer un fichier** : Cliquez sur le bouton "Supprimer" d'un fichier
5. **Résumer un PDF avec IA** : Cliquez sur "Résumer avec IA" pour un fichier PDF

### API REST

#### Lister les fichiers
```bash
GET http://localhost:4567/list
```

#### Upload un fichier
```bash
POST http://localhost:4567/upload
Content-Type: multipart/form-data

file: [fichier]
```

#### Télécharger un fichier
```bash
GET http://localhost:4567/download/:filename
```

#### Supprimer un fichier
```bash
DELETE http://localhost:4567/delete/:filename
```

### API SOAP

Le service SOAP expose les mêmes opérations via WSDL :
- WSDL disponible à : **http://localhost:8089/cloud?wsdl**
- Opérations : `listFiles()`, `upload()`, `download()`, `delete()`

### API AI

#### Résumer un PDF
```bash
POST http://localhost:8000/summarize
Content-Type: application/json

{
  "filename": "nom_du_fichier.pdf"
}
```

**Réponse** :
```json
{
  "summary": "Résumé généré par l'IA..."
}
```

## 📁 Structure du projet

```
projet_soa/
│
├── AI/                          # Service Python pour l'IA
│   ├── __pycache__/
│   ├── genai.py                # Configuration Google Gemini AI
│   ├── main.py                 # Fonctions d'extraction et résumé PDF
│   ├── operation_logger.py     # Logger Python pour les opérations
│   ├── server.py               # Serveur FastAPI
│   └── requirements.txt        # Dépendances Python
│
├── CloudWS/                     # Backend Java
│   ├── MiniCloudStorage/       # Dossier de stockage des fichiers
│   │   ├── *.pdf               # Fichiers uploadés
│   │   └── ...
│   ├── operations_log.xml      # Log des opérations (généré)
│   ├── operations_log.xsd      # Schéma XML pour le log
│   ├── pom.xml                 # Configuration Maven
│   ├── src/
│   │   └── main/
│   │       └── java/
│   │           └── cloud/
│   │               ├── logging/
│   │               │   └── OperationLogger.java
│   │               ├── rest/
│   │               │   └── RestServer.java
│   │               ├── soap/
│   │               │   ├── server/
│   │               │   │   └── SoapServer.java
│   │               │   └── service/
│   │               │       ├── CloudService.java
│   │               │       └── ICloud.java
│   │               └── storage/
│   │                   └── CloudStorage.java
│   └── target/                 # Fichiers compilés (généré)
│
└── mini-cloud-react/           # Frontend React
    ├── node_modules/           # Dépendances npm (généré)
    ├── public/
    ├── src/
    │   ├── components/
    │   │   ├── CloudClient.js   # Composant principal
    │   │   └── CloudClient.css
    │   ├── App.js
    │   ├── App.css
    │   └── index.js
    ├── package.json
    └── README.md
```

## 🔌 API Endpoints

### REST API (Port 4567)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/list` | Liste tous les fichiers |
| POST | `/upload` | Upload un fichier |
| GET | `/download/:f` | Télécharge un fichier |
| DELETE | `/delete/:f` | Supprime un fichier |

### AI API (Port 8000)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/` | Informations sur le service |
| POST | `/summarize` | Résume un fichier PDF |

### SOAP API (Port 8089)

- WSDL : `http://localhost:8089/cloud?wsdl`
- Opérations : `listFiles`, `upload`, `download`, `delete`

## 🐛 Dépannage

### Le serveur REST ne démarre pas

**Problème** : Erreur de classpath ou dépendances manquantes

**Solution** :
```bash
cd CloudWS
mvn clean install
# Vérifiez que target/MiniCloud_SOAP_REST-1.0-SNAPSHOT.jar existe
```

### Le service AI ne fonctionne pas

**Problème 1** : Erreur "GEMINI_API_KEY not found"

**Solution** :
- Vérifiez que le fichier `.env` existe dans `AI/`
- Vérifiez que la clé API est correctement définie : `GEMINI_API_KEY=votre_cle`

**Problème 2** : Erreur lors du résumé de PDF

**Solution** :
- Vérifiez que le fichier PDF existe dans `CloudWS/MiniCloudStorage/`
- Vérifiez que le fichier est un PDF valide
- Vérifiez les logs du serveur FastAPI pour plus de détails

### Le frontend ne se connecte pas au backend

**Problème** : Erreur CORS ou connexion refusée

**Solution** :
- Vérifiez que le serveur REST est démarré sur le port 4567
- Vérifiez que le service AI est démarré sur le port 8000
- Vérifiez les URLs dans `mini-cloud-react/src/components/CloudClient.js` :
  - `SERVER_URL = "http://127.0.0.1:4567"`
  - `AI_API_URL = "http://127.0.0.1:8000"`

### Erreur "Port already in use"

**Problème** : Un port est déjà utilisé par un autre processus

**Solution** :
- Windows : `netstat -ano | findstr :4567` puis `taskkill /PID <pid> /F`
- Linux/Mac : `lsof -ti:4567 | xargs kill -9`
- Ou changez les ports dans les fichiers de configuration

### Les fichiers ne s'affichent pas

**Problème** : Le dossier MiniCloudStorage n'existe pas ou est vide

**Solution** :
- Vérifiez que le dossier `CloudWS/MiniCloudStorage/` existe
- Le dossier est créé automatiquement au premier démarrage
- Upload un fichier pour vérifier que le stockage fonctionne

### Erreur Maven

**Problème** : Dépendances Maven non téléchargées

**Solution** :
```bash
cd CloudWS
mvn clean
mvn dependency:resolve
mvn install
```

### Erreur npm

**Problème** : Dépendances npm corrompues

**Solution** :
```bash
cd mini-cloud-react
rm -rf node_modules package-lock.json
npm install
```

## 📝 Notes importantes

1. **Ordre de démarrage** : Il est recommandé de démarrer les services dans cet ordre :
   - 1. Service REST (Java)
   - 2. Service AI (Python)
   - 3. Frontend React

2. **Fichiers de log** : Les opérations sont journalisées dans `CloudWS/operations_log.xml`

3. **Stockage** : Tous les fichiers sont stockés dans `CloudWS/MiniCloudStorage/`

4. **Sécurité** : En production, configurez correctement CORS et ajoutez une authentification

5. **Clé API** : Ne commitez jamais votre fichier `.env` contenant la clé API dans le contrôle de version

## 👥 Auteur

**Racem Dammak**

## 📄 Licence

Ce projet est fourni tel quel pour des fins éducatives.

---

**Bon développement ! 🚀**

