package cloud.storage;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class CloudStorage {

    private static final String STORAGE = "MiniCloudStorage";

    public CloudStorage() {
        File dir = new File(STORAGE);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public String[] listFiles() {
        File folder = new File(STORAGE);
        File[] files = folder.listFiles();
        if (files == null) {
            return new String[0];
        }
        List<String> fileNames = new ArrayList<>();
        for (File file : files) {
            if (file.isFile()) {
                fileNames.add(file.getName());
            }
        }
        return fileNames.toArray(new String[0]);
    }

    public boolean upload(String filename, byte[] data) {
        if (filename == null || filename.isEmpty() || data == null) {
            return false;
        }
        try {
            Path filePath = Paths.get(STORAGE, filename);
            Files.write(filePath, data);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public byte[] download(String filename) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }
        try {
            Path filePath = Paths.get(STORAGE, filename);
            if (!Files.exists(filePath)) {
                return null;
            }
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean delete(String filename) {
        if (filename == null || filename.isEmpty()) {
            return false;
        }
        try {
            Path path = Paths.get(STORAGE, filename);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
