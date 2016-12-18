package es.deusto.ssdd.jms.message;

/**
 * Created by .local on 15/11/2016.
 */
public interface IJMSMessage {

    /**
     * Piece of code that triggers when a new message is received from a tracker instance
     * This code is executed in destination node
     *
     * @param destinationNodeId
     */
    void onReceivedEvent(String destinationNodeId);

    /**
     * Piece of code that triggers when a new message is sent from a tracker instance.
     * This code is executed in source node
     * @param
     */
    void onBroadcastEvent(String currentNodeId);

    String getSourceTrackerId();

    String getPrintable();
}
