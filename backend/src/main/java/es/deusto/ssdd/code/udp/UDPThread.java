package es.deusto.ssdd.code.udp;

import java.net.ServerSocket;

/**
 * Created by .local on 15/12/2016.
 */
public class UDPThread extends Thread {

    private boolean active;
    private ServerSocket soc;
    private TrackerUDPServer ms;

    public UDPThread(TrackerUDPServer mainServer, ServerSocket soc) {
        this.soc = soc;
        active = true;
        ms = mainServer;
    }

    @Override
    public void run() {
        while (active) {
            SocketManager sockManager;
            try {
                sockManager = new SocketManager(soc.accept());
                Request request = new Request(ms, sockManager);
                Thread thread = new Thread(request);
                thread.start();
                // soc.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public ServerSocket getServerSocket() {
        return soc;
    }

    public void setServerSocket(ServerSocket soc) {
        this.soc = soc;
    }

}