package nl.kb.dare.model.preproces;

import nl.kb.dare.model.SocketNotifier;
import nl.kb.dare.model.statuscodes.ProcessStatus;
import nl.kb.dare.nbn.NumbersController;
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
import java.util.stream.IntStream;

public class RecordBatchLoader {

    private final Map<Integer, List<Record>> batchMap = Collections.synchronizedMap(new HashMap<>());

    private final RecordDao recordDao;
    private final NumbersController numbersController;
    private final RecordReporter recordReporter;
    private final SocketNotifier socketNotifier;

    public RecordBatchLoader(RecordDao recordDao, NumbersController numbersController, RecordReporter recordReporter,
                             SocketNotifier socketNotifier) {

        this.recordDao = recordDao;
        this.numbersController = numbersController;
        this.recordReporter = recordReporter;
        this.socketNotifier = socketNotifier;
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

        final List<Record> records = batchMap.get(repositoryId);
        final List<Long> numbers = numbersController.getNumbers(records.size());
        IntStream.range(0, records.size()).forEach(idx ->
                records.get(idx).setKbObjId(numbers.get(idx)));

        synchronized (recordDao) {
            recordDao.insertBatch(records);
        }
        records.clear();

        socketNotifier.notifyUpdate(recordReporter.getStatusUpdate());
    }
}
