package nl.kb.dare.model.preproces;

import nl.kb.dare.model.statuscodes.ProcessStatus;
import nl.kb.oaipmh.OaiRecordHeader;
import nl.kb.oaipmh.OaiStatus;

public class Record {

    private Long id;
    private ProcessStatus state;
    private String ipName;
    private String tsCreate;
    private String tsProcessed;
    private Integer repositoryId;
    private String oaiIdentifier;
    private final String oaiDateStamp;

    private Record(RecordBuilder recordBuilder) {
        this.id = recordBuilder.id;
        this.state = recordBuilder.state;
        this.ipName = recordBuilder.ipName;
        this.repositoryId = recordBuilder.repositoryId;
        this.oaiIdentifier = recordBuilder.oaiIdentifier;
        this.oaiDateStamp = recordBuilder.oaiDateStamp;
        this.tsCreate = recordBuilder.tsCreate;
        this.tsProcessed = recordBuilder.tsProcessed;
    }

    static Record fromHeader(OaiRecordHeader header, Integer repositoryId) {
        return new RecordBuilder()
                .setState(header.getOaiStatus() == OaiStatus.AVAILABLE ? ProcessStatus.PENDING : ProcessStatus.DELETED)
                .setIpName(null)
                .setRepositoryId(repositoryId)
                .setOaiIdentifier(header.getIdentifier())
                .setOaiDateStamp(header.getDateStamp())
                .createRecord();
    }

    public Integer getState() {
        return state.getCode();
    }

    public String getIpName() {
        return ipName;
    }

    public Integer getRepositoryId() {
        return repositoryId;
    }

    public void setIpName(Long ipName) {
        this.ipName = String.format("%d", ipName);
    }

    public void setState(ProcessStatus processStatus) {
        this.state = processStatus;
    }

    public Long getId() {
        return id;
    }

    public String getOaiIdentifier() {
        return oaiIdentifier;
    }

    public String getOaiDateStamp() { return oaiDateStamp; }


    public String getTsCreate() {
        return tsCreate;
    }

    public String getTsProcessed() {
        return tsProcessed;
    }

    static class RecordBuilder {
        private Long id = null;
        private ProcessStatus state;
        private String ipName;
        private Integer repositoryId;
        private String oaiIdentifier;
        private String oaiDateStamp;
        private String tsCreate = null;
        private String tsProcessed = null;

        RecordBuilder setId(Long id) {
            this.id = id;
            return this;
        }

        RecordBuilder setState(ProcessStatus state) {
            this.state = state;
            return this;
        }

        RecordBuilder setIpName(String ipName) {
            this.ipName = ipName;
            return this;
        }

        RecordBuilder setRepositoryId(Integer repositoryId) {
            this.repositoryId = repositoryId;
            return this;
        }

        RecordBuilder setOaiIdentifier(String oaiIdentifier) {
            this.oaiIdentifier = oaiIdentifier;
            return this;
        }

        RecordBuilder setOaiDateStamp(String oaiDateStamp) {
            this.oaiDateStamp = oaiDateStamp;
            return this;
        }

        RecordBuilder setTsCreate(String tsCreate) {
            this.tsCreate = tsCreate;
            return this;
        }

        RecordBuilder setTsProcessed(String tsProcessed) {
            this.tsProcessed = tsProcessed;
            return this;
        }

        public Long getId() {
            return id;
        }

        public ProcessStatus getState() {
            return state;
        }

        public String getIpName() {
            return ipName;
        }

        public Integer getRepositoryId() {
            return repositoryId;
        }

        public String getOaiIdentifier() {
            return oaiIdentifier;
        }

        public String getOaiDateStamp() {
            return oaiDateStamp;
        }

        public String getTsCreate() {
            return tsCreate;
        }

        public String getTsProcessed() {
            return tsProcessed;
        }

        Record createRecord() {
            return new Record(this);
        }
    }
}
