package es.deusto.ssdd.code.net.jms;


/**
 * Created by .local on 08/11/2016.
 */
public class TrackerMainThread {

    public static void main(String[] args) throws Exception {
        new TrackerInstance().start();
        new TrackerInstance().start();
    }
}
