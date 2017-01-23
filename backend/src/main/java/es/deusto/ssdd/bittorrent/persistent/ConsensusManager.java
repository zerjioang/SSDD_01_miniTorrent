package es.deusto.ssdd.bittorrent.persistent;

import es.deusto.ssdd.jms.TrackerInstance;

/**
 * Created by .local on 22/01/2017.
 */
public class ConsensusManager {

    private final TrackerInstance trackerInstance;
    private Thread backgroundThread;

    private boolean keepRunning;

    private boolean needToUpdate;

    public ConsensusManager(TrackerInstance trackerInstance) {
        this.trackerInstance = trackerInstance;
        this.keepRunning = true;
    }

    public void startOnBackground() {
        backgroundThread = new Thread(() -> {
            trackerInstance.addLogLine("persistence: Consensus Manager started as background service");
            while (keepRunning) {

            }
        });
        backgroundThread.start();
    }

    public boolean isKeepRunning() {
        return keepRunning;
    }

    public void setKeepRunning(boolean keepRunning) {
        this.keepRunning = keepRunning;
    }
}
