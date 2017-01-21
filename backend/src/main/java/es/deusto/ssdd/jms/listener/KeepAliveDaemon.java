package es.deusto.ssdd.jms.listener;

import es.deusto.ssdd.jms.TrackerInstance;
import es.deusto.ssdd.jms.message.MessageCollection;
import es.deusto.ssdd.jms.model.TrackerDaemonSpec;

import javax.jms.JMSException;
import java.util.ArrayList;

/**
 * Created by .local on 18/11/2016.
 */
public class KeepAliveDaemon implements Runnable {

    public static final int KEEP_ALIVE_FREQUENCY_MS = 2000;

    private final TrackerInstance daemonOwner;

    public KeepAliveDaemon(TrackerInstance daemonOwner) throws IllegalArgumentException {
        if (daemonOwner == null) {
            throw new IllegalArgumentException("Tracker instance owner of a keep alive daemon cannot be null");
        }
        this.daemonOwner = daemonOwner;
    }

    @Override
    public void run() {
        daemonOwner.addLogLine("Debug: KEEP_ALIVE Daemon ACTIVE");
        try {
            while (daemonOwner.isAlive()) {
                sendKeepAliveMessage();
                updateOtherNodesKeepAliveStatus();
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
        daemonOwner.addLogLine("Debug: KEEP_ALIVE Daemon STOPPED");
    }

    private void updateOtherNodesKeepAliveStatus() {
        daemonOwner.addLogLine("Debug updating tracker node list keep alive status");

        ArrayList<TrackerInstance> instancesToRemove = new ArrayList<>();

        for (TrackerInstance instance : daemonOwner.getTrackerNodesAsList()) {
            if (instance.isKeepAliveCountDead()) {
                instancesToRemove.add(instance);
            } else {
                instance.decreasePendingLifeTime();
            }
        }
        if (!instancesToRemove.isEmpty()) {
            daemonOwner.removeDeadNodesFromList(instancesToRemove);
        }
    }

    private void sendKeepAliveMessage() throws JMSException {
        JMSMessageSender sender = daemonOwner.getSender(TrackerDaemonSpec.KEEP_ALIVE_SERVICE);
        sender.send(MessageCollection.KEEP_ALIVE);
        try {
            Thread.sleep(KEEP_ALIVE_FREQUENCY_MS);
        } catch (InterruptedException e) {
        }
    }
}
