package es.deusto.ssdd.code.net.jms.message.wrapper;

import es.deusto.ssdd.code.net.jms.TrackerInstance;
import es.deusto.ssdd.code.net.jms.message.IJMSMessage;

import java.io.Serializable;

/**
 * Created by .local on 14/11/2016.
 */
public class DataSyncMessage implements Serializable, IJMSMessage {

    private final String remoteNodeId;

    public DataSyncMessage(String trackerId) {
        this.remoteNodeId = trackerId;
    }

    @Override
    public void onReceivedEvent(String currentNodeId) {
        TrackerInstance thisNode = TrackerInstance.getNode(currentNodeId);
        TrackerInstance remoteNode = TrackerInstance.getNode(remoteNodeId);
        if (thisNode != null) {
            thisNode.requestDatabaseClone(remoteNode);
        }
    }

    @Override
    public void onBroadcastEvent() {
        System.out.println(remoteNodeId + " Tracker data sync message broadcast event here");
    }

    @Override
    public String getSourceTrackerId() {
        return this.remoteNodeId;
    }

    @Override
    public String getPrintable() {
        return "DATA SYNC from " + this.getSourceTrackerId();
    }
}
