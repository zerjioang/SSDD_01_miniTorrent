package es.deusto.ssdd.code.net.jms;


/**
 * Created by .local on 08/11/2016.
 */
public class NodeClusterTrackerDemo {

    public static void main(String[] args) throws Exception {
        new TrackerInstance().deploy();
        new TrackerInstance().deploy();
        new TrackerInstance().deploy();
        new TrackerInstance().deploy();
        //new TrackerInstance().deploy();
        //new TrackerInstance().deploy();

    }
}
