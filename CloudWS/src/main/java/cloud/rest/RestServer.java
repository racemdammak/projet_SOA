package cloud.rest;

import cloud.storage.CloudStorage;
import cloud.logging.OperationLogger;
import spark.Spark;
import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;

public class RestServer {

    static CloudStorage storage = new CloudStorage();
    static Gson gson = new Gson();

    public static void main(String[] args) {
        Spark.port(4567);

        Spark.before((req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Methods", "GET,POST,DELETE,OPTIONS");
            res.header("Access-Control-Allow-Headers", "*");
        });

        Spark.options("/*", (req, res) -> {
            return "";
        });

        // Lister les fichiers
        Spark.get("/list", (req, res) -> {
            res.type("application/json");
            String[] files = storage.listFiles();
            return gson.toJson(files);
        });

        // Upload un fichier
        Spark.post("/upload", (req, res) -> {
            res.type("application/json");
            String filename = null;
            byte[] data = null;
            String contentType = req.contentType();
            if (contentType != null && contentType.contains("multipart/form-data")) {
                try {
                    req.raw().setAttribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/tmp"));
                    Part filePart = req.raw().getPart("file");
                    if (filePart == null) {
                        filePart = req.raw().getPart("upload");
                    }
                    if (filePart != null) {
                        filename = filePart.getSubmittedFileName();
                        if (filename == null || filename.isEmpty()) {
                            filename = req.queryParams("filename");
                        }
                        try (java.io.InputStream inputStream = filePart.getInputStream()) {
                            java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
                            byte[] temp = new byte[4096];
                            int bytesRead;
                            while ((bytesRead = inputStream.read(temp)) != -1) {
                                buffer.write(temp, 0, bytesRead);
                            }
                            data = buffer.toByteArray();
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing multipart: " + e.getMessage());
                }
            }

            if (contentType != null && contentType.contains("application/json") && (filename == null || data == null)) {
                try {
                    String body = req.body();
                    Map<?, ?> jsonBody = gson.fromJson(body, Map.class);
                    if (filename == null || filename.isEmpty()) {
                        filename = (String) jsonBody.get("filename");
                        if (filename == null) {
                            filename = (String) jsonBody.get("name");
                        }
                    }

                    if (data == null || data.length == 0) {
                        String base64Data = (String) jsonBody.get("data");
                        if (base64Data == null) {
                            base64Data = (String) jsonBody.get("file");
                        }
                        if (base64Data != null && !base64Data.isEmpty()) {
                            data = java.util.Base64.getDecoder().decode(base64Data);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing JSON: " + e.getMessage());
                }
            }

            if (filename == null || filename.isEmpty()) {
                filename = req.queryParams("filename");
                if (filename == null) {
                    filename = req.queryParams("name");
                }
            }

            if (data == null || data.length == 0) {
                data = req.bodyAsBytes();
            }

            if (filename == null || filename.isEmpty()) {
                res.status(400);
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Filename is required");
                error.put("details", "Provide filename via: query param 'filename', or JSON field 'filename' or 'name'");
                error.put("contentType", contentType);
                error.put("hasBody", req.body() != null && !req.body().isEmpty());
                return gson.toJson(error);
            }

            if (data == null || data.length == 0) {
                res.status(400);
                Map<String, Object> error = new HashMap<>();
                error.put("error", "File data is required");
                error.put("details", "Provide file data in request body (raw bytes) or as base64 in JSON field 'data' or 'file'");
                error.put("contentType", contentType);
                error.put("bodyLength", req.body() != null ? req.body().length() : 0);
                return gson.toJson(error);
            }

            boolean success = storage.upload(filename, data);
            Map<String, Object> response = new HashMap<>();
            if (success) {
                OperationLogger.logOperation(
                    OperationLogger.OperationType.UPLOAD,
                    filename,
                    OperationLogger.OperationStatus.SUCCESS,
                    "Fichier uploadé avec succès",
                    (long) data.length
                );
                response.put("status", "success");
                response.put("message", "Fichier uploadé avec succès");
                response.put("filename", filename);
                response.put("size", data.length);
                return gson.toJson(response);
            } else {
                OperationLogger.logOperation(
                    OperationLogger.OperationType.UPLOAD,
                    filename,
                    OperationLogger.OperationStatus.ERROR,
                    "Upload du fichier échoué",
                    null
                );
                res.status(500);
                response.put("status", "error");
                response.put("message", "Upload du fichier échoué");
                return gson.toJson(response);
            }
        });

        // Télécharger un fichier
        Spark.get("/download/:f", (req, res) -> {
            String filename = req.params("f");
            byte[] data = storage.download(filename);
            if (data != null && data.length > 0) {
                OperationLogger.logOperation(
                    OperationLogger.OperationType.TELECHARGER,
                    filename,
                    OperationLogger.OperationStatus.SUCCESS,
                    "Fichier téléchargé avec succès",
                    (long) data.length
                );
                res.type("application/octet-stream");
                res.header("Content-Disposition", "attachment; filename=\"" + filename + "\"");
                res.raw().getOutputStream().write(data);
                res.raw().getOutputStream().flush();
                return "";
            } else {
                OperationLogger.logOperation(
                    OperationLogger.OperationType.TELECHARGER,
                    filename,
                    OperationLogger.OperationStatus.ERROR,
                    "Télchargement du fichier échoué",
                    null
                );
                res.status(404);
                res.type("application/json");
                Map<String, Object> error = new HashMap<>();
                error.put("status", "error");
                error.put("message", "Téléchargement du fichier échoué");
                error.put("filename", filename);
                return gson.toJson(error);
            }
        });

        // Supprimer un fichier
        Spark.delete("/delete/:f", (req, res) -> {
            String filename = req.params("f");
            boolean success = storage.delete(filename);
            res.type("application/json");
            Map<String, Object> response = new HashMap<>();
            if (success) {
                OperationLogger.logOperation(
                    OperationLogger.OperationType.SUPPRIMER,
                    filename,
                    OperationLogger.OperationStatus.SUCCESS,
                    "Fichier supprimé avec succès",
                    null
                );
                response.put("status", "success");
                response.put("message", "Fichier supprimé avec succès");
                response.put("filename", filename);
            } else {
                OperationLogger.logOperation(
                    OperationLogger.OperationType.SUPPRIMER,
                    filename,
                    OperationLogger.OperationStatus.ERROR,
                    "Suppression du fichier échouée",
                    null
                );
                res.status(404);
                response.put("status", "error");
                response.put("message", "Suppression du fichier échouée");
                response.put("filename", filename);
            }
            return gson.toJson(response);
        });
        System.out.println("REST Server running at http://localhost:4567");
    }
}
