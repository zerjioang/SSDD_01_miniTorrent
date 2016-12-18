package es.deusto.ssdd.code.udp;

import es.deusto.ssdd.jms.TrackerInstance;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Created by .local on 15/12/2016.
 */
public class TrackerUDPServer {

    private static final int MIN_PORT = 1000;
    private static final int MAX_PORT = 5000;
    private final TrackerInstance trackerInstance;
    private int port;
    private UDPThread handler;
    private ServerSocket welcomeSocket;

    public TrackerUDPServer(TrackerInstance trackerInstance) {
        this.trackerInstance = trackerInstance;
        this.port = MIN_PORT + (int)(Math.random() * ((MAX_PORT - MIN_PORT) + 1));
    }

    public void backgroundDispatch() {
        System.out.println(trackerInstance.getTrackerId()+" [Starting UDP server on port "+port+"]");
        startServer();
    }

    private void startServer() {
        try {
            welcomeSocket = new ServerSocket(port);
            System.out.println("socket online");
            System.out.println("Waiting for clients to connect...");

            handler = new UDPThread(this, welcomeSocket);
            handler.start();

        } catch (IOException e) {
            System.err.println("Error handling client request.");
            System.err.println("Error details: "+e.getLocalizedMessage());
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public int getListeningPort() {
        return port;
    }

    public void stopService() {
        //stop the main listener thread
        this.handler.setActive(false);
        //close the socket
        try {
            welcomeSocket.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
        System.out.println(trackerInstance.getTrackerId() + " UDP Server stopped");
    }
}
