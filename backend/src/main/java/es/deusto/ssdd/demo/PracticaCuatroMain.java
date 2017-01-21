package es.deusto.ssdd.demo;


import es.deusto.ssdd.client.gui.view.ClientWindow;
import es.deusto.ssdd.client.udp.client.PeerClient;
import es.deusto.ssdd.jms.TrackerInstance;

public class PracticaCuatroMain {

    private static final int NODES = 1;

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < NODES; i++) {
            TrackerInstance tracker = new TrackerInstance();
            tracker.deploy();
        }
        //se simula un cliente udp
        PeerClient client = new PeerClient();

        //se le asigna una ventana a ese peer
        ClientWindow clientWindow = new ClientWindow(client);
        clientWindow.setVisible(true);

        //se connecta
        client.connect();
    }
}
