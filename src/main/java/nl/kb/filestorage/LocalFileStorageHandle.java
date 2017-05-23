package nl.kb.filestorage;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

class LocalFileStorageHandle implements FileStorageHandle {
    private static final int MAX_ENTRIES = 10_000;
    private static final LinkedHashMap<String, LocalFileStorageHandle> instances = new LinkedHashMap<String, LocalFileStorageHandle>(){
        protected boolean removeEldestEntry(Map.Entry<String, LocalFileStorageHandle> eldest) {
            return size() > MAX_ENTRIES;
        }
    };

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
        final String reversedId = new StringBuilder(identifier).reverse().toString();

        try {
            if (reversedId.length() < 3) {
                // code not expected to be reached, all identifiers are expected to be greater than 3 characters
                return String.format("%s/%s__short_id", basePath,
                        URLEncoder.encode(identifier, "UTF-8"));
            } else {
                return String.format("%s/%s/%s/%s/%s", basePath,
                        URLEncoder.encode(reversedId.substring(0, 1), "UTF-8"),
                        URLEncoder.encode(reversedId.substring(1, 2), "UTF-8"),
                        URLEncoder.encode(reversedId.substring(2, 3), "UTF-8"),
                        URLEncoder.encode(identifier, "UTF-8"));
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Panic!! unsupported encoding UTF-8", e);
        }
    }

    @Override
    public FileStorageHandle create() throws IOException {
        FileUtils.forceMkdir(new File(fileDir));
        return this;
    }

    @Override
    public FileStorageHandle clear() throws IOException {
        FileUtils.cleanDirectory(new File(fileDir));
        return this;
    }

    @Override
    public OutputStream getOutputStream(String filename) throws IOException {
        return new FileOutputStream(new File(String.format("%s/%s", fileDir, filename)));
    }

    @Override
    public OutputStream getOutputStream(String path, String filename) throws IOException {
        final String filePath = String.format("%s/%s", fileDir, path);
        FileUtils.forceMkdir(new File(filePath));
        return new FileOutputStream(new File(String.format("%s/%s", filePath, filename)));
    }

    @Override
    public InputStream getFile(String filename) throws FileNotFoundException {
        return new FileInputStream(new File(String.format("%s/%s", fileDir, filename)));
    }

    @Override
    public void deleteFiles() throws IOException {
        FileUtils.deleteDirectory(new File(fileDir));
    }

    @Override
    public void downloadZip(OutputStream output) throws IOException {
        final ZipOutputStream zipOutputStream = new ZipOutputStream(output);

        zipFile(zipOutputStream, "metadata.xml");
        zipFile(zipOutputStream, "manifest.xml");

        final File resourceDir = new File(fileDir + "/resources");
        if (resourceDir.exists() && resourceDir.isDirectory()) {
            for (File file : FileUtils.listFiles(resourceDir, null, false)) {
                zipFile(zipOutputStream, String.format("resources/%s", file.getName()));
            }
        }

        zipOutputStream.close();
    }

    private void zipFile(ZipOutputStream zipOutputStream, String name) throws IOException {

        try {
            final InputStream is = getFile(name);
            final ZipEntry metadataEntry = new ZipEntry(name);
            zipOutputStream.putNextEntry(metadataEntry);
            IOUtils.copy(is, zipOutputStream);
            is.close();
        } catch (FileNotFoundException e) {
            return;
        }
    }
}
