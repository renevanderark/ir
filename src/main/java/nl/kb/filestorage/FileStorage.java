package nl.kb.filestorage;

import java.io.IOException;

/**
 * Represents a place to store files in
 */
public interface FileStorage {

    /**
     * Creates a handle to store files with based on an identifier
     * @param identifier the identifier
     * @return instance of {@link FileStorageHandle}
     * @throws IOException
     */
    FileStorageHandle create(String identifier) throws IOException;
}
