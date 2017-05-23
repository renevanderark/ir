package nl.kb.dare.model.preproces;

import nl.kb.dare.model.statuscodes.ProcessStatus;
import nl.kb.oaipmh.OaiRecordHeader;
import nl.kb.oaipmh.OaiStatus;

public class Record {

    private Integer id;
    private ProcessStatus state;
    private String kbObjId;
    private String tsCreate;
    private String tsProcessed;
    private String fingerprint;
    private Integer repositoryId;
    private String oaiIdentifier;

    Record(Integer id, ProcessStatus state, String kbObjId, String fingerprint, Integer repositoryId, String oaiIdentifier) {
        this.id = id;
        this.state = state;
        this.kbObjId = kbObjId;
        this.repositoryId = repositoryId;
        this.fingerprint = fingerprint;
        this.oaiIdentifier = oaiIdentifier;
    }

    private Record(ProcessStatus state, String kbObjId, String fingerprint, Integer repositoryId, String oaiIdentifier) {
        this(null, state, kbObjId, fingerprint, repositoryId, oaiIdentifier);
    }

    static Record fromHeader(OaiRecordHeader header, Integer repositoryId) {
        return new Record(
                header.getOaiStatus() == OaiStatus.AVAILABLE ? ProcessStatus.PENDING : ProcessStatus.SKIP,
                "TODO: number generator",
                header.getFingerprint(),
                repositoryId,
                header.getIdentifier()
        );
    }

    public Integer getState() {
        return state.getCode();
    }

    public String getKbObjId() {
        return kbObjId;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public Integer getRepositoryId() {
        return repositoryId;
    }

    public void setKbObjId(Long kbObjId) {
        this.kbObjId = String.format("%d", kbObjId);
    }

    public void setState(ProcessStatus processStatus) {
        this.state = processStatus;
    }

    public Integer getId() {
        return id;
    }

    public String getOaiIdentifier() {
        return oaiIdentifier;
    }
}
