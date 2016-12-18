package es.deusto.ssdd.jms.message.wrapper;

import es.deusto.ssdd.jms.TrackerInstance;
import es.deusto.ssdd.jms.message.IJMSMessage;

import java.io.Serializable;

/**
 * Created by .local on 14/11/2016.
 */
public class KeepAliveMessage implements Serializable, IJMSMessage {

    private final String remoteNodeId;

    public KeepAliveMessage(String trackerId) {
        this.remoteNodeId = trackerId;
    }

    @Override
    public void onReceivedEvent(String currentNodeId) {
        TrackerInstance remoteNode = TrackerInstance.getNode(remoteNodeId);
        TrackerInstance thisNode = TrackerInstance.getNode(currentNodeId);
        updateNodeLifeStatus(thisNode, remoteNode);
        addNodeToTrackerNodeList(thisNode, remoteNode);
    }

    private void addNodeToTrackerNodeList(TrackerInstance thisNode, TrackerInstance remoteNode) {
        if (remoteNode != null) {
            boolean alreadyDiscovered = thisNode.isAlreadyDiscovered(remoteNode);
            if (!alreadyDiscovered) {
                thisNode.addRemoteNode(remoteNode);
            }
            thisNode.updateNodeTable(thisNode.getTrackerNodeList());
        }
    }

    private void updateNodeLifeStatus(TrackerInstance thisNode, TrackerInstance remoteNode) {
        if (remoteNode != null) {
            thisNode.resetRemoteNodeTimeInLocalRegistry(remoteNode);
        }
    }

    @Override
    public void onBroadcastEvent(String currentNodeId) {
    }

    @Override
    public String getSourceTrackerId() {
        return this.remoteNodeId;
    }

    @Override
    public String getPrintable() {
        return "KEEP ALIVE from " + this.getSourceTrackerId();
    }
}
