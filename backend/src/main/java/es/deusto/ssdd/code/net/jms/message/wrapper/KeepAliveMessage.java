package es.deusto.ssdd.code.net.jms.message.wrapper;

import es.deusto.ssdd.code.net.jms.TrackerInstance;
import es.deusto.ssdd.code.net.jms.message.IJMSMessage;

import java.io.Serializable;

/**
 * Created by .local on 14/11/2016.
 */
public class KeepAliveMessage implements Serializable, IJMSMessage {

    private final String trackerId;

    public KeepAliveMessage(String trackerId) {
        this.trackerId = trackerId;
    }

    @Override
    public void onReceivedEvent(String destinationNodeId) {
        TrackerInstance node = TrackerInstance.getNode(destinationNodeId);
        if (node != null) {
            node.resetNodeLifeCountdown();
        }
    }

    @Override
    public void onBroadcastEvent() {
    }

    @Override
    public String getSourceTrackerId() {
        return this.trackerId;
    }

    @Override
    public String getPrintable() {
        return "KEEP ALIVE from " + this.getSourceTrackerId();
    }
}
