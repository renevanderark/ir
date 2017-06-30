package nl.kb.dare;

import com.google.common.util.concurrent.AbstractScheduledService;
import io.dropwizard.lifecycle.Managed;

class ManagedPeriodicTask implements Managed {


    private final AbstractScheduledService periodicTask;

    ManagedPeriodicTask(AbstractScheduledService periodicTask) {
        this.periodicTask = periodicTask;
    }

    @Override
    public void start() throws Exception {
        periodicTask.startAsync().awaitRunning();
    }

    @Override
    public void stop() throws Exception {
        periodicTask.stopAsync().awaitTerminated();
    }
}
