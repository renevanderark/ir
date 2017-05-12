package nl.kb.manifest;

// Represents a file node in Manifest, used to download and ship files with
public class ObjectResource {

    private String id;
    private String xlinkHref;
    private String checksum;
    private String checksumType;
    private String localFilename;
    private long size;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getXlinkHref() {
        return xlinkHref;
    }

    public void setXlinkHref(String xlinkHref) {
        this.xlinkHref = xlinkHref;
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
}
