package es.deusto.ssdd.jms.message.wrapper;

import es.deusto.ssdd.jms.TrackerInstance;
import es.deusto.ssdd.jms.message.IJMSMessage;

import java.io.Serializable;

public class DataSyncMessage implements Serializable, IJMSMessage {

    private final String sourceTrackerId;
    private final String remoteTrackerId;
    private String query;

    public DataSyncMessage(String sourceTrackerId, String remoteTrackerId) {
        this.sourceTrackerId = sourceTrackerId;
        this.remoteTrackerId = remoteTrackerId;
        this.query = null;
    }

    @Override
    public void onReceivedEvent(String currentNodeId) {
        TrackerInstance remoteNode = TrackerInstance.getNode(sourceTrackerId);

        //proces message only, if it is for me
        if (currentNodeId.equals(remoteTrackerId)) {
            TrackerInstance thisNode = TrackerInstance.getNode(currentNodeId);
            if (thisNode != null && this.query != null) {
                //update data sync request on local storage
                thisNode.syncData(query);
            }
        }
    }

    @Override
    public void onBroadcastEvent(String currentNodeId) {
        System.out.println(currentNodeId + " DATA_SYNC trigger ");
    }

    @Override
    public String getSourceTrackerId() {
        return this.sourceTrackerId;
    }

    @Override
    public String getPrintable() {
        return "DATA_SYNC from " + this.getSourceTrackerId();
    }
}
