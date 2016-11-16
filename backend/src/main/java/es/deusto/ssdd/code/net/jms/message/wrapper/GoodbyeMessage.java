package es.deusto.ssdd.code.net.jms.message.wrapper;

import es.deusto.ssdd.code.net.jms.TrackerInstance;
import es.deusto.ssdd.code.net.jms.message.IJMSMessage;

import java.io.Serializable;

/**
 * Created by .local on 14/11/2016.
 */
public class GoodbyeMessage implements Serializable, IJMSMessage {

    private final String trackerId;

    public GoodbyeMessage(String trackerId) {
        this.trackerId = trackerId;
    }

    @Override
    public void onReceivedEvent(String myLocalId) {
        TrackerInstance thisNode = TrackerInstance.getNode(myLocalId);
        TrackerInstance remoteNode = TrackerInstance.getNode(trackerId);
        if (thisNode != null) {
            thisNode.removeRemoteNode(trackerId);
            if (remoteNode.isMaster()) {
                System.out.println(myLocalId + " MASTER is gone. Election time!!");
                thisNode.beginMasterElectionProcess();
            }
        }
    }

    @Override
    public void onBroadcastEvent() {
        System.out.println(trackerId + " Tracker goodbye message broadcast event here");
    }

    @Override
    public String getSourceTrackerId() {
        return this.trackerId;
    }

    @Override
    public String getPrintable() {
        return "GOOD BYE from " + this.getSourceTrackerId();
    }
}
