package es.deusto.ssdd.demo;


import es.deusto.ssdd.client.udp.client.PeerClient;
import es.deusto.ssdd.tracker.jms.TrackerInstance;

public class PracticaTresMain {

    private static final int NODES = 2;

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < NODES; i++) {
            TrackerInstance tracker = new TrackerInstance();
            tracker.deploy();
        }
        //se simula un cliente udp
        PeerClient client = new PeerClient();
        //this method simulates a file sharing communication. Use it for testing connection and response messages
        client.test();
    }
}
