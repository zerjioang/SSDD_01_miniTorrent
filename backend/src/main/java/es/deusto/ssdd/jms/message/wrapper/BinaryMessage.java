package es.deusto.ssdd.jms.message.wrapper;

import es.deusto.ssdd.jms.TrackerInstance;
import es.deusto.ssdd.jms.message.IJMSMessage;

import java.io.Serializable;

/**
 * Created by .local on 14/11/2016.
 */
public class BinaryMessage implements Serializable, IJMSMessage {

    private final String sourceTrackerId;
    private final String remoteTrackerId;
    private byte[] binaryContent;

    public BinaryMessage(String sourceTrackerId, String remoteTrackerId, byte[] binaryContent) {
        this.sourceTrackerId = sourceTrackerId;
        this.remoteTrackerId = remoteTrackerId;
        this.binaryContent = binaryContent;
    }

    @Override
    public void onReceivedEvent(String currentNodeId) {
        TrackerInstance thisNode = TrackerInstance.getNode(currentNodeId);
        TrackerInstance remoteNode = TrackerInstance.getNode(sourceTrackerId);
        //read message, only, if it is for me
        if (remoteTrackerId.equals(currentNodeId)) {
            //message is for me. otherwise, drop
            if (thisNode != null) {
                //read binary content
                System.out.println(thisNode.getTrackerId() + " Received: " + binaryContent.length + " bytes of binary message");
                thisNode.overwriteLocalDatabase(binaryContent);
            }
        }
    }

    @Override
    public void onBroadcastEvent() {
        System.out.println(sourceTrackerId + " Tracker data sync message broadcast event here");
    }

    @Override
    public String getSourceTrackerId() {
        return this.sourceTrackerId;
    }

    @Override
    public String getPrintable() {
        return "BINARY MESSAGE from " + this.getSourceTrackerId();
    }
}
