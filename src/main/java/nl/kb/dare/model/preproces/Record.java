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

    private Record(ProcessStatus state, String kbObjId, String fingerprint, Integer repositoryId) {
        this.state = state;
        this.kbObjId = kbObjId;
        this.repositoryId = repositoryId;
        this.tsCreate = tsCreate;
        this.tsProcessed = tsProcessed;
        this.fingerprint = fingerprint;
    }

    static Record fromHeader(OaiRecordHeader header, Integer repositoryId) {
        return new Record(
                header.getOaiStatus() == OaiStatus.AVAILABLE ? ProcessStatus.PENDING : ProcessStatus.SKIP,
                "TODO: number generator",
                header.getFingerprint(),
                repositoryId
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
}
