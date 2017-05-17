package nl.kb.dare.model.preproces;

import nl.kb.dare.model.SocketNotifier;
import nl.kb.dare.nbn.NumbersController;
import nl.kb.oaipmh.OaiRecordHeader;
import nl.kb.oaipmh.OaiStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class RecordBatchLoader {

    private final List<Record> batch = Collections.synchronizedList(new ArrayList<>());

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

    public synchronized void addToBatch(Integer repositoryId, OaiRecordHeader oaiRecordHeader) {

        if (oaiRecordHeader.getOaiStatus() == OaiStatus.AVAILABLE
                && !recordDao.existsByFingerPrint(oaiRecordHeader)) {
            batch.add(Record.fromHeader(oaiRecordHeader, repositoryId));
        }

        if (batch.size() == 100) {
            flushBatch();
        }
    }

    public synchronized void flushBatch() {
        if (batch.size() == 0) {
            return;
        }

        final List<Long> numbers = numbersController.getNumbers(batch.size());
        IntStream.range(0, batch.size()).forEach(idx -> batch.get(idx).setKbObjId(numbers.get(idx)));
        recordDao.insertBatch(batch);
        batch.clear();

        socketNotifier.notifyUpdate(recordReporter.getStatusUpdate());
    }
}
