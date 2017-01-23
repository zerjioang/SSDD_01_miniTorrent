package es.deusto.ssdd.tracker.jms.message.wrapper;

import es.deusto.ssdd.tracker.jms.TrackerInstance;
import es.deusto.ssdd.tracker.jms.message.IJMSMessage;

import java.io.Serializable;

/**
 * Created by .local on 14/11/2016.
 */
public class GoodbyeMessage implements Serializable, IJMSMessage {

    private final String remoteNodeId;

    public GoodbyeMessage(String trackerId) {
        this.remoteNodeId = trackerId;
    }

    @Override
    public void onReceivedEvent(String currentNodeId) {
        TrackerInstance thisNode = TrackerInstance.getNode(currentNodeId);
        TrackerInstance remoteNode = TrackerInstance.getNode(remoteNodeId);
        if (thisNode != null) {
            thisNode.removeRemoteNode(remoteNodeId);
            if (remoteNode.isMaster()) {
                thisNode.addLogLine("MASTER is gone. Election time!!");
                thisNode.beginMasterElectionProcess();
            }
        }
    }

    @Override
    public void onBroadcastEvent(String currentNodeId) {
        TrackerInstance thisNode = TrackerInstance.getNode(currentNodeId);
        thisNode.addLogLine("Stream: tracker goodbye message sent. Stopping");
        thisNode.stopNode();
    }

    @Override
    public String getSourceTrackerId() {
        return this.remoteNodeId;
    }

    @Override
    public String getPrintable() {
        return "GOOD BYE from " + this.getSourceTrackerId();
    }
}
