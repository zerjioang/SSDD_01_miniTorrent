package es.deusto.ssdd.code.net.jms.message;

/**
 * Created by .local on 15/11/2016.
 */
public interface IJMSMessage {

    void onReceivedEvent(String destinationNodeId);

    void onBroadcastEvent();

    String getSourceTrackerId();

    String getPrintable();
}
