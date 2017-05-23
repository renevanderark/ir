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

    @JsonProperty
    private String sampleFileDir;

    public FileStorage getFileStorage(String dir) {
        switch (storageType) {
            case "local":
                final File fStorageDir = new File(dir);
                if (!fStorageDir.exists()) { throw new RuntimeException("Directory does not exist: " + dir); }
                if (!fStorageDir.isDirectory()) { throw new RuntimeException("File is not a directory: " + dir); }
                if (!fStorageDir.canWrite()) { throw new RuntimeException("No write permissions for directory: " + dir); }
                return new LocalFileStorage(dir);
            default:
                throw new RuntimeException("Unsupported file storage type");
        }
    }

    public FileStorage getFileStorage() {
        return getFileStorage(storageDir);
    }

    public FileStorage sampleFileStorage() {
        return getFileStorage(sampleFileDir);
    }
}
