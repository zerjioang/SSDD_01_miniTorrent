package es.deusto.ssdd.tracker.jms.message.wrapper;

import es.deusto.ssdd.tracker.jms.TrackerInstance;
import es.deusto.ssdd.tracker.jms.message.IJMSMessage;

import java.io.Serializable;

public class DataSyncMessage implements Serializable, IJMSMessage {

    private final String sourceTrackerId;
    private final String remoteTrackerId;
    private String payload;

    public DataSyncMessage(String sourceTrackerId, String remoteTrackerId) {
        this.sourceTrackerId = sourceTrackerId;
        this.remoteTrackerId = remoteTrackerId;
        this.payload = null;
    }

    @Override
    public void onReceivedEvent(String currentNodeId) {
        TrackerInstance remoteNode = TrackerInstance.getNode(sourceTrackerId);

        //proces message only, if it is for me
        if (currentNodeId.equals(remoteTrackerId)) {
            TrackerInstance thisNode = TrackerInstance.getNode(currentNodeId);
            if (thisNode != null && this.payload != null) {
                //update data sync request on local storage
                if(thisNode.canSaveData()){
                    thisNode.syncData(payload);
                    thisNode.addLogLine("Stream: data sync message received from " + sourceTrackerId);
                }
            }
        }
    }

    @Override
    public void onBroadcastEvent(String currentNodeId) {
        TrackerInstance.getNode(currentNodeId).addLogLine("Stream: DATA_SYNC trigger ");
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
