package nl.kb.dare.model.preproces;

import nl.kb.oaipmh.OaiRecordHeader;
import nl.kb.oaipmh.OaiStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecordBatchLoader {

    private final List<Record> batch = Collections.synchronizedList(new ArrayList<>());

    private final RecordDao recordDao;

    public RecordBatchLoader(RecordDao recordDao) {
        this.recordDao = recordDao;
    }

    public synchronized void addToBatch(Integer repositoryId, OaiRecordHeader oaiRecordHeader) {

        if (oaiRecordHeader.getOaiStatus() == OaiStatus.AVAILABLE && recordDao.countByFingerprint(oaiRecordHeader) == 0) {
            batch.add(Record.fromHeader(oaiRecordHeader, repositoryId));
        }

        if (batch.size() == 100) {
            flushBatch();
        }
    }

    public synchronized void flushBatch() {
        recordDao.insertBatch(batch);
        batch.clear();
    }
}
