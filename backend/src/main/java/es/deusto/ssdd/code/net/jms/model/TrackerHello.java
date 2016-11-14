package es.deusto.ssdd.code.net.jms.model;

import java.io.Serializable;

/**
 * Created by .local on 14/11/2016.
 */
public class TrackerHello implements Serializable {

    private final String trackerId;

    public TrackerHello(String trackerId) {
        this.trackerId = trackerId;
    }

    public String getTrackerId() {
        return trackerId;
    }

    public boolean isMine(String trackerId) {
        return this.trackerId.equals(trackerId);
    }
}
