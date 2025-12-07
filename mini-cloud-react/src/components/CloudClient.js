import React, { useState, useEffect, useCallback } from "react";
import { useDropzone } from "react-dropzone";
import { ToastContainer, toast } from "react-toastify";
import ReactMarkdown from "react-markdown";
import "react-toastify/dist/ReactToastify.css";
import "./CloudClient.css";

function CloudClient() {
  const [filesList, setFilesList] = useState([]);
  const [isSummaryModalOpen, setIsSummaryModalOpen] = useState(false);
  const [summary, setSummary] = useState("");
  const [isLoadingSummary, setIsLoadingSummary] = useState(false);
  const [selectedFileName, setSelectedFileName] = useState("");
  const SERVER_URL = "http://127.0.0.1:4567";
  const AI_API_URL = "http://127.0.0.1:8000";

  // Gestion du drag & drop
  const onDrop = useCallback(async (acceptedFiles) => {
    for (let file of acceptedFiles) {
      const formData = new FormData();
      formData.append("filename", file.name);
      formData.append("file", file);

      try {
        const res = await fetch(`${SERVER_URL}/upload`, {
          method: "POST",
          body: formData,
        });
        if (!res.ok) throw new Error("Erreur upload serveur");
        toast.success(`Upload OK: ${file.name}`);
      } catch (err) {
        console.error(err);
        toast.error(`Erreur upload: ${file.name}`);
      }
    }
    fetchFiles();
  }, []);

  const { getRootProps, getInputProps, isDragActive } = useDropzone({ onDrop });

  // Lister les fichiers
  const fetchFiles = async () => {
    try {
      const res = await fetch(`${SERVER_URL}/list`);
      if (!res.ok) throw new Error("Erreur récupération fichiers");
      const text = await res.text();
      let parsedFiles = JSON.parse(text.replace(/'/g, '"'));
      
      // Handle different response formats
      // If files are objects, extract the filename property
      if (Array.isArray(parsedFiles)) {
        parsedFiles = parsedFiles.map(file => {
          if (typeof file === 'object' && file !== null) {
            // Try common property names for filename
            return file.name || file.filename || file.file || JSON.stringify(file);
          }
          return file;
        }).filter(file => file !== null && file !== undefined);
      }
      
      setFilesList(parsedFiles);
    } catch (err) {
      console.error("Error fetching files:", err);
      toast.error("Erreur récupération liste fichiers");
    }
  };

  // Télécharger un fichier
  const downloadFile = async (filename) => {
    try {
      const res = await fetch(`${SERVER_URL}/download/${filename}`);
      if (!res.ok) throw new Error("Fichier non trouvé");
      const blob = await res.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = filename;
      a.click();
      window.URL.revokeObjectURL(url);
      toast.success(`Téléchargé: ${filename}`);
    } catch (err) {
      console.error(err);
      toast.error(`Erreur téléchargement: ${filename}`);
    }
  };

  // Supprimer un fichier
  const deleteFile = async (filename) => {
    if (!window.confirm(`Êtes-vous sûr de vouloir supprimer "${filename}" ?`)) {
      return;
    }
    try {
      const res = await fetch(`${SERVER_URL}/delete/${filename}`, {
        method: "DELETE",
      });
      if (!res.ok) throw new Error("Erreur suppression serveur");
      toast.success(`Supprimé: ${filename}`);
      fetchFiles();
    } catch (err) {
      console.error(err);
      toast.error(`Erreur suppression: ${filename}`);
    }
  };

  // Résumer un fichier avec IA
  const summarizeFile = async (filename) => {
    setIsLoadingSummary(true);
    setSelectedFileName(filename);
    setIsSummaryModalOpen(true);
    setSummary("");

    try {
      const res = await fetch(`${AI_API_URL}/summarize`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          filename: filename,
        }),
      });

      if (!res.ok) {
        const errorData = await res.json().catch(() => ({ detail: res.statusText }));
        throw new Error(errorData.detail || `HTTP ${res.status}: ${res.statusText}`);
      }
      
      const data = await res.json();
      setSummary(data.summary || data.text || "Résumé généré avec succès");
      toast.success("Résumé généré avec succès");
    } catch (err) {
      console.error("Erreur résumé:", err);
      const errorMessage = err.message || "Erreur lors de la génération du résumé";
      setSummary(`Erreur: ${errorMessage}\n\nVeuillez vérifier que:\n- Le serveur FastAPI est démarré sur le port 8000\n- Le fichier existe dans le stockage\n- Le fichier est un PDF valide`);
      toast.error(`Erreur: ${errorMessage}`);
    } finally {
      setIsLoadingSummary(false);
    }
  };

  // Fermer le modal
  const closeSummaryModal = () => {
    setIsSummaryModalOpen(false);
    setSummary("");
    setSelectedFileName("");
  };

  useEffect(() => {
    fetchFiles();
  }, []);

  const formatFileSize = (bytes) => {
    if (!bytes) return "N/A";
    if (bytes === 0) return "0 Bytes";
    const k = 1024;
    const sizes = ["Bytes", "KB", "MB", "GB"];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round(bytes / Math.pow(k, i) * 100) / 100 + " " + sizes[i];
  };

  return (
    <div className="cloud-client">
      <div className="cloud-header">
        <div className="header-content">
          <h1 className="app-title">
            <span className="cloud-icon">☁️</span>
            MiniCloud
          </h1>
          <p className="app-subtitle">Gérez vos fichiers en toute simplicité</p>
        </div>
      </div>

      <div className="cloud-container">
        {/* Zone Drag & Drop */}
        <div
          {...getRootProps()}
          className={`dropzone ${isDragActive ? "active" : ""}`}
        >
          <input {...getInputProps()} />
          <div className="dropzone-content">
            <div className="dropzone-icon">
              {isDragActive ? "📤" : "📁"}
            </div>
            {isDragActive ? (
              <p className="dropzone-text">Déposez vos fichiers ici...</p>
            ) : (
              <>
                <p className="dropzone-text">
                  Glissez-déposez des fichiers ici
                </p>
                <p className="dropzone-subtext">
                  ou cliquez pour sélectionner
                </p>
              </>
            )}
          </div>
        </div>

        {/* Liste des fichiers */}
        <div className="files-section">
          <div className="files-header">
            <h2 className="files-title">
              <span className="files-icon">📋</span>
              Fichiers disponibles
            </h2>
            <button className="refresh-btn" onClick={fetchFiles} title="Actualiser">
              🔄
            </button>
          </div>

          {filesList.length === 0 ? (
            <div className="empty-state">
              <div className="empty-icon">📭</div>
              <p className="empty-text">Aucun fichier trouvé</p>
              <p className="empty-subtext">
                Commencez par téléverser un fichier
              </p>
            </div>
          ) : (
            <div className="files-grid">
              {filesList.map((f, index) => {
                // Extract filename - handle both string and object formats
                const filename = typeof f === 'string' ? f : (f?.name || f?.filename || f?.file || String(f) || `file-${index}`);
                
                return (
                  <div key={filename || index} className="file-card">
                    <div className="file-icon">📄</div>
                    <div className="file-info">
                      <h3 className="file-name" title={filename}>
                        {filename}
                      </h3>
                      <p className="file-path">/{filename}</p>
                    </div>
                    <div className="file-actions">
                      <button
                        className="btn btn-summarize"
                        onClick={() => summarizeFile(filename)}
                        title="Résumer avec IA"
                      >
                        <span className="btn-icon">🤖</span>
                        <span className="btn-text">Résumer avec IA</span>
                      </button>
                      <button
                        className="btn btn-download"
                        onClick={() => downloadFile(filename)}
                        title="Télécharger"
                      >
                        <span className="btn-icon">⬇️</span>
                        <span className="btn-text">Télécharger</span>
                      </button>
                      <button
                        className="btn btn-delete"
                        onClick={() => deleteFile(filename)}
                        title="Supprimer"
                      >
                        <span className="btn-icon">🗑️</span>
                        <span className="btn-text">Supprimer</span>
                      </button>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>

      {/* Modal de résumé */}
      {isSummaryModalOpen && (
        <div className="summary-modal-overlay" onClick={closeSummaryModal}>
          <div className="summary-modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="summary-modal-header">
              <h2 className="summary-modal-title">
                <span className="summary-icon">🤖</span>
                Résumé IA - {selectedFileName}
              </h2>
              <button className="summary-modal-close" onClick={closeSummaryModal}>
                ✕
              </button>
            </div>
            <div className="summary-modal-body">
              {isLoadingSummary ? (
                <div className="summary-loading">
                  <div className="loading-spinner"></div>
                  <p>Génération du résumé en cours...</p>
                </div>
              ) : (
                <div className="summary-text">
                  {summary ? (
                    <ReactMarkdown>{summary}</ReactMarkdown>
                  ) : (
                    "Aucun résumé disponible"
                  )}
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      <ToastContainer
        position="top-right"
        autoClose={3000}
        hideProgressBar={false}
        newestOnTop={false}
        closeOnClick
        rtl={false}
        pauseOnFocusLoss
        draggable
        pauseOnHover
        theme="colored"
      />
    </div>
  );
}

export default CloudClient;
