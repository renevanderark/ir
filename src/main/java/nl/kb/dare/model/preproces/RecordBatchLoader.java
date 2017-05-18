package nl.kb.dare.model.preproces;

import nl.kb.dare.model.SocketNotifier;
import nl.kb.dare.nbn.NumbersController;
import nl.kb.oaipmh.OaiRecordHeader;
import nl.kb.oaipmh.OaiStatus;

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
            batchMap.put(repositoryId, new ArrayList<>());
        }
        if (oaiRecordHeader.getOaiStatus() == OaiStatus.AVAILABLE
                && !recordDao.existsByFingerPrint(oaiRecordHeader)) {
            batchMap.get(repositoryId).add(Record.fromHeader(oaiRecordHeader, repositoryId));
        }
    }

    public void flushBatch(Integer repositoryId) {
        if (!batchMap.containsKey(repositoryId) || batchMap.get(repositoryId).size() == 0) {
            return;
        }

        final List<Record> records = batchMap.get(repositoryId);
        final List<Long> numbers = numbersController.getNumbers(records.size());
        IntStream.range(0, batchMap.size()).forEach(idx ->
                records.get(idx).setKbObjId(numbers.get(idx)));

        synchronized (recordDao) {
            recordDao.insertBatch(records);
        }

        records.clear();

        socketNotifier.notifyUpdate(recordReporter.getStatusUpdate());
    }
}
