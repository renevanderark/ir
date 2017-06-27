package nl.kb.dare.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.kb.filestorage.FileStorage;
import nl.kb.filestorage.LocalFileStorage;

import java.io.File;

public class FileStorageFactory {
    @JsonProperty
    private String storageType;

    @JsonProperty
    private String storageDir;

    private FileStorage getFileStorage(String dir) {
        if (storageType == null || storageType.equals("local")) {
            final File fStorageDir = new File(dir);
            if (!fStorageDir.exists()) {
                throw new IllegalStateException("Directory does not exist: " + dir);
            }
            if (!fStorageDir.isDirectory()) {
                throw new IllegalStateException("File is not a directory: " + dir);
            }
            if (!fStorageDir.canWrite()) {
                throw new IllegalStateException("No write permissions for directory: " + dir);
            }
            return new LocalFileStorage(dir);
        } else {
            throw new IllegalStateException("Unsupported file storage type");
        }
    }

    public FileStorage getFileStorage() {
        return getFileStorage(storageDir);
    }

}
