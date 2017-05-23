package nl.kb.filestorage;

import java.io.IOException;

public class LocalFileStorage implements FileStorage {
    private final String storageDir;

    public LocalFileStorage(String storageDir) {
        this.storageDir = storageDir;
    }

    @Override
    public FileStorageHandle create(String identifier) throws IOException {
        return LocalFileStorageHandle.getInstance(identifier, storageDir)
                .create();
    }
}
