package es.deusto.ssdd.bittorrent.persistent;

import es.deusto.ssdd.jms.TrackerInstance;

import java.util.ArrayList;

/**
 * Created by .local on 22/01/2017.
 */
public class ConsensusManager {

    private final TrackerInstance trackerInstance;
    private Thread backgroundThread;

    private boolean keepRunning;

    private boolean needToUpdate;

    private ArrayList<String> queries;

    public ConsensusManager(TrackerInstance trackerInstance) {
        this.trackerInstance = trackerInstance;
        this.keepRunning = true;
        this.queries = new ArrayList<>();
    }

    public void startOnBackground() {
        backgroundThread = new Thread(() -> {
            trackerInstance.addLogLine("persistence: Consensus Manager started as background service");
            while (keepRunning) {
                int idx = 0;
                while (!queries.isEmpty()) {
                    boolean result = updateLocalStorage(queries.get(idx));
                    if (result) {
                        this.queries.remove(idx);
                    }
                    idx = result ? idx + 1 : idx;
                }
            }
        });
        backgroundThread.start();
    }

    private boolean updateLocalStorage(String query) {
        PersistenceHandler handler = trackerInstance.getPersistenceHandler();
        return handler.sync(query);
    }


    public boolean isKeepRunning() {
        return keepRunning;
    }

    public void setKeepRunning(boolean keepRunning) {
        this.keepRunning = keepRunning;
    }
}
