package nl.kb.filestorage;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;

class LocalFileStorageHandle implements FileStorageHandle {
    private static final int MAX_ENTRIES = 10_000;
    private static final LinkedHashMap<String, LocalFileStorageHandle> instances = new LinkedHashMap<String, LocalFileStorageHandle>(){
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, LocalFileStorageHandle> eldest) {
            return size() > MAX_ENTRIES;
        }
    };
    private static final String NESTED_PATH_FMT = "%s/%s";

    private String getFileDir() {
        return fileDir;
    }

    private final String fileDir;

    private LocalFileStorageHandle(String fileDir) {
        this.fileDir = fileDir;
    }

    static synchronized LocalFileStorageHandle getInstance(String identifier, String basePath) {
        final String filePath = getFilePath(identifier, basePath);
        if (!instances.containsKey(filePath)) {
            instances.put(filePath, new LocalFileStorageHandle(filePath));
        }
        return instances.get(filePath);
    }

    static String getFilePath(String identifier, String basePath) {
        return String.format("%s/%s", basePath, identifier);
    }

    @Override
    public FileStorageHandle create() throws IOException {
        FileUtils.forceMkdir(new File(fileDir));
        return this;
    }

    @Override
    public OutputStream getOutputStream(String filename) throws IOException {
        return new FileOutputStream(new File(String.format(NESTED_PATH_FMT, fileDir, filename)));
    }

    @Override
    public OutputStream getOutputStream(String path, String filename) throws IOException {
        final String filePath = String.format(NESTED_PATH_FMT, fileDir, path);
        FileUtils.forceMkdir(new File(filePath));
        return new FileOutputStream(new File(String.format(NESTED_PATH_FMT, filePath, filename)));
    }

    @Override
    public InputStream getFile(String filename) throws FileNotFoundException {
        return new FileInputStream(new File(String.format(NESTED_PATH_FMT, fileDir, filename)));
    }

    @Override
    public void moveTo(FileStorageHandle other) throws IOException {
        if (other != null && other instanceof LocalFileStorageHandle) {
            FileUtils.moveDirectory(new File(this.fileDir), new File(
                    ((LocalFileStorageHandle)other).getFileDir()));
        }
    }

}
