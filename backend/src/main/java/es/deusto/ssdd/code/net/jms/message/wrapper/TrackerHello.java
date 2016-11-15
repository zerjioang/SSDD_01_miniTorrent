package es.deusto.ssdd.code.net.jms.message.wrapper;

import es.deusto.ssdd.code.net.jms.TrackerInstance;
import es.deusto.ssdd.code.net.jms.message.IJMSMessage;

import java.io.Serializable;

/**
 * Created by .local on 14/11/2016.
 */
public class TrackerHello implements Serializable, IJMSMessage {

    private final String trackerId;

    public TrackerHello(String trackerId) {
        this.trackerId = trackerId;
    }

    @Override
    public void onReceivedEvent() {
        System.out.println(getSourceTrackerId()+" :: Adding discovered new node to local collection");
        TrackerInstance node = TrackerInstance.getNode(trackerId);
        if(node!=null){
            node.addRemoteNode(getSourceTrackerId());
        }
    }

    @Override
    public void onBroadcastEvent() {
        System.out.println("Tracker hello message broadcast event here");
    }

    @Override
    public String getSourceTrackerId() {
        return this.trackerId;
    }

    @Override
    public String getPrintable() {
        return "HELLO WORLD from "+this.getSourceTrackerId();
    }
}
