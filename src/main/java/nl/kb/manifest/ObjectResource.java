package nl.kb.manifest;

// Represents a file node in Manifest, used to download and ship files with
public class ObjectResource {

    private String id;
    private String downloadUrl;
    private String checksum;
    private String checksumType;
    private String checksumDate; // = download date as well
    private String localFilename;
    private long size;
    private String contentDisposition;
    private String contentType;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public void setChecksumType(String checksumType) {
        this.checksumType = checksumType;
    }

    public String getChecksum() {
        return checksum;
    }

    public String getChecksumType() {
        return checksumType;
    }

    public void setLocalFilename(String localFilename) {
        this.localFilename = localFilename;
    }

    public String getLocalFilename() {
        return localFilename;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getSize() {
        return size;
    }

    public void setContentDisposition(String contentDisposition) {
        this.contentDisposition = contentDisposition;
    }

    public String getContentDisposition() {
        return contentDisposition;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContentType() {
        return contentType;
    }

    public String getChecksumDate() {
        return checksumDate;
    }

    public void setChecksumDate(String checksumDate) {
        this.checksumDate = checksumDate;
    }
}
