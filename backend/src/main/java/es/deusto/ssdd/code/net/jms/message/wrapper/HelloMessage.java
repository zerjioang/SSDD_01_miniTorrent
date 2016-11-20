package es.deusto.ssdd.code.net.jms.message.wrapper;

import es.deusto.ssdd.code.net.jms.TrackerInstance;
import es.deusto.ssdd.code.net.jms.message.IJMSMessage;

import java.io.Serializable;

/**
 * Created by .local on 14/11/2016.
 */
public class HelloMessage implements Serializable, IJMSMessage {

    private final String remoteNodeId;

    public HelloMessage(String trackerId) {
        this.remoteNodeId = trackerId;
    }

    @Override
    public void onReceivedEvent(String currentId) {
        TrackerInstance thisNode = TrackerInstance.getNode(currentId);
        TrackerInstance remoteNode = TrackerInstance.getNode(remoteNodeId);
        if (remoteNode != null) {
            thisNode.addRemoteNode(remoteNode);
            thisNode.updateNodeTable(thisNode.getTrackerNodeList());
        }
        thisNode.beginMasterElectionProcess();
        thisNode._debug_election_result();
    }

    @Override
    public void onBroadcastEvent() {
    }

    @Override
    public String getSourceTrackerId() {
        return this.remoteNodeId;
    }

    @Override
    public String getPrintable() {
        return "HELLO WORLD from " + this.getSourceTrackerId();
    }
}
