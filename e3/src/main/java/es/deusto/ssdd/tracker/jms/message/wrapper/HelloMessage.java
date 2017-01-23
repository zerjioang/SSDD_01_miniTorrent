package es.deusto.ssdd.tracker.jms.message.wrapper;

import es.deusto.ssdd.tracker.jms.TrackerInstance;
import es.deusto.ssdd.tracker.jms.message.IJMSMessage;

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
        }
        thisNode.beginMasterElectionProcess();
        thisNode._debug_election_result();
        //when a new hello_world message is received, we also need to send a message back of type clone database
        thisNode.sendDatabaseBack(remoteNode);
    }

    @Override
    public void onBroadcastEvent(String currentNodeId) {
        TrackerInstance.getNode(currentNodeId).addLogLine("Stream: HELLO MESSAGE SENT");
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
