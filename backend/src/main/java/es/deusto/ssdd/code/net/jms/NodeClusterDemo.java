package es.deusto.ssdd.code.net.jms;


/**
 * Created by .local on 08/11/2016.
 */
public class NodeClusterDemo {

    public static void main(String[] args) throws Exception {
        new TrackerInstance().sayHelloToTrackerNodesCluster();
        new TrackerInstance().sayHelloToTrackerNodesCluster();
        new TrackerInstance().sayHelloToTrackerNodesCluster();
        new TrackerInstance().sayHelloToTrackerNodesCluster();
        new TrackerInstance().sayHelloToTrackerNodesCluster();
        new TrackerInstance().sayHelloToTrackerNodesCluster();
    }
}
