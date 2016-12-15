package es.deusto.ssdd.code.udp;

import java.io.IOException;

/**
 * Created by .local on 15/12/2016.
 */
final class Request implements Runnable {

    private SocketManager sockManager;
    private TrackerUDPServer ms;

    // Constructor
    public Request(TrackerUDPServer ms, SocketManager sockMan) throws Exception {
        sockManager = sockMan;
        this.ms = ms;
    }

    // Implement the run() method of the Runnable interface.
    @Override
    public synchronized void run() {
        try {
            String requestLine = sockManager.read();
            // aqui viene toda la logica de negocio del server
            if (requestLine != null) {
                System.out.println("[received data] " + requestLine);
                // process data
                ClientRequestParser crq = new ClientRequestParser(
                        sockManager,
                        ms);
                crq.setClientRequest(requestLine);
                crq.parse();
                //responder
                crq.responder();
                // Close streams and socket.
                sockManager.closeStreams();
                sockManager.closeSocket();
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}