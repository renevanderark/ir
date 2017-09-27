package nl.kb.filestorage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Represents a place to store files in
 */
public interface FileStorageHandle {

    FileStorageHandle create() throws IOException;

    OutputStream getOutputStream(String filename) throws IOException;
    OutputStream getOutputStream(String path, String filename) throws IOException;
    InputStream getFile(String filename) throws FileNotFoundException;

    void moveTo(FileStorageHandle fileStorageHandle) throws IOException;
}
