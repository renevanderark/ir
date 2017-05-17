package nl.kb.dare.model.preproces;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.kb.dare.model.SocketNotifier;
import nl.kb.oaipmh.OaiRecordHeader;
import nl.kb.oaipmh.OaiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecordBatchLoader {
    private static final Logger LOG = LoggerFactory.getLogger(RecordBatchLoader.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final List<Record> batch = Collections.synchronizedList(new ArrayList<>());

    private final RecordDao recordDao;
    private final RecordReporter recordReporter;
    private final SocketNotifier socketNotifier;

    public RecordBatchLoader(RecordDao recordDao, RecordReporter recordReporter, SocketNotifier socketNotifier) {
        this.recordDao = recordDao;
        this.recordReporter = recordReporter;
        this.socketNotifier = socketNotifier;
    }

    public synchronized void addToBatch(Integer repositoryId, OaiRecordHeader oaiRecordHeader) {

        if (oaiRecordHeader.getOaiStatus() == OaiStatus.AVAILABLE
                /* TODO, is check needed?: && recordDao.countByFingerprint(oaiRecordHeader) == 0 */) {
            batch.add(Record.fromHeader(oaiRecordHeader, repositoryId));
        }

        if (batch.size() == 100) {
            flushBatch();
        }
    }

    public synchronized void flushBatch() {
        recordDao.insertBatch(batch);
        batch.clear();
        socketNotifier.notifyUpdate(recordReporter.getStatusUpdate());
    }
}
