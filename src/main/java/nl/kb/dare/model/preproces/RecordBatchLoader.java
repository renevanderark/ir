package nl.kb.dare.model.preproces;

import nl.kb.dare.model.repository.RepositoryDao;
import nl.kb.dare.model.statuscodes.ProcessStatus;
import nl.kb.dare.idgen.IdGenerator;
import nl.kb.dare.websocket.SocketNotifier;
import nl.kb.http.HttpResponseException;
import nl.kb.oaipmh.OaiRecordHeader;
import nl.kb.oaipmh.OaiStatus;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RecordBatchLoader {

    private final Map<Integer, List<Record>> batchMap = Collections.synchronizedMap(new HashMap<>());

    private final RecordDao recordDao;
    private final RepositoryDao repositoryDao;
    private final IdGenerator idGenerator;
    private final RecordReporter recordReporter;
    private final SocketNotifier socketNotifier;
    private final Boolean batchLoadSampleMode;

    public RecordBatchLoader(RecordDao recordDao, RepositoryDao repositoryDao, IdGenerator idGenerator,
                             RecordReporter recordReporter, SocketNotifier socketNotifier,
                             Boolean batchLoadSampleMode) {

        this.recordDao = recordDao;
        this.repositoryDao = repositoryDao;
        this.idGenerator = idGenerator;
        this.recordReporter = recordReporter;
        this.socketNotifier = socketNotifier;
        this.batchLoadSampleMode = batchLoadSampleMode;
    }

    public void addToBatch(Integer repositoryId, OaiRecordHeader oaiRecordHeader) {

        if (!batchMap.containsKey(repositoryId)) {
            batchMap.put(repositoryId, Collections.synchronizedList(new ArrayList<>()));
        }

        if (oaiRecordHeader.getOaiStatus() == OaiStatus.AVAILABLE
                && !recordDao.existsByDatestampAndIdentifier(oaiRecordHeader)) {
            batchMap.get(repositoryId).add(Record.fromHeader(oaiRecordHeader, repositoryId));
        }

        // DARE2017-9 when a record exists as PENDING or FAILED, which is later deleted, set the status to deleted
        if (oaiRecordHeader.getOaiStatus() == OaiStatus.DELETED) {
            final Record existing = recordDao.findByOaiId(oaiRecordHeader.getIdentifier());
            if (existing != null && (
                    existing.getState() == ProcessStatus.PENDING.getCode() ||
                    existing.getState() == ProcessStatus.FAILED.getCode()
            )) {
                existing.setState(ProcessStatus.DELETED);
                synchronized (recordDao) {
                    recordDao.updateState(existing);
                }
            }
        }
    }

    public void flushBatch(Integer repositoryId) throws SAXException, IOException, HttpResponseException {
        if (!batchMap.containsKey(repositoryId) || batchMap.get(repositoryId).isEmpty()) {
            return;
        }
        final String prefix = repositoryDao.findById(repositoryId).getSet().replaceAll(":.*$", "");
        final List<Record> records = batchMap.get(repositoryId);
        final List<String> uniqueIdentifiers = idGenerator.getUniqueIdentifiers(records.size());

        IntStream.range(0, records.size()).forEach(idx ->
                records.get(idx).setIpName(String.format("%s_%s", prefix, uniqueIdentifiers.get(idx))));

        synchronized (recordDao) {
            if (batchLoadSampleMode) {
                recordDao.insertBatch(records.stream().limit(1).collect(Collectors.toList()));
            } else {
                recordDao.insertBatch(new ArrayList<>(records));
            }
        }
        records.clear();

        socketNotifier.notifyUpdate(recordReporter.getStatusUpdate());
    }

}
