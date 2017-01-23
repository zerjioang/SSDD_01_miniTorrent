package es.deusto.ssdd.tracker.udp;

import bittorrent.udp.AnnounceRequest;
import bittorrent.udp.BitTorrentUDPRequestMessage;
import bittorrent.udp.ScrapeInfo;
import es.deusto.ssdd.tracker.jms.TrackerInstance;
import es.deusto.ssdd.tracker.udp.parser.PeerRequestParser;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.List;

/**
 * Created by .local on 15/12/2016.
 */
public class TrackerUDPServer {

    private static final int MIN_PORT = 1000;
    private static final int MAX_PORT = 5000;
    private static final long TWO_MINUTES_MILLIS = 2 * 60 * 1000;
    /*
    In IPv4, any IP address in the range 224.0.0.0 to 239.255.255.255 can be used as a multicast address to send a datagram packet.
    The IP address 224.0.0.0 is reserved and you should not use it in your application.
     */
    private static final String MULTICAST_IP = "224.0.0.1";
    private final TrackerInstance trackerInstance;
    private final String ip;
    private int port;
    private MulticastSocket serverSocket;
    private HashMap<Long, Long> connectionIdList;
    private boolean multicastEnabled;
    private boolean keepServerAlive;

    public TrackerUDPServer(TrackerInstance trackerInstance, String ip) {
        this.trackerInstance = trackerInstance;
        this.ip = ip;
        this.port = MIN_PORT + (int) (Math.random() * ((MAX_PORT - MIN_PORT) + 1));
        this.port = 1234;
        this.connectionIdList = new HashMap<>();
        this.multicastEnabled = true;
        this.keepServerAlive = true;
    }

    public void backgroundDispatch() throws IOException {
        new Thread(() -> {
            this.trackerInstance.addLogLine("Debug: [Starting UDP server on port " + port + "]");
            try {
                startServer();
            } catch (IOException e) {
                this.trackerInstance.addLogLine("Error: " + e.getLocalizedMessage());
            }
        }).start();
    }

    private void startServer() throws IOException {
        if (multicastEnabled) {
            startMulticastServer();
        } else {
            startStandardServer();
        }
    }

    private void startMulticastServer() throws IOException {
        InetAddress mcIPAddress = null;
        mcIPAddress = InetAddress.getByName(MULTICAST_IP);
        serverSocket = new MulticastSocket(this.port);
        this.trackerInstance.addLogLine("Debug: Multicast UDP server running at:" + serverSocket.getLocalSocketAddress());
        //joint ot multicast group
        serverSocket.joinGroup(mcIPAddress);
        //buffer
        byte[] receiveData = new byte[1024];
        //start listening
        while (keepServerAlive) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            //get es.deusto.ssdd.client address for response
            InetAddress clientAddress;
            clientAddress = receivePacket.getAddress();
            int clientPort = receivePacket.getPort();
            //read received data as bytearray
            byte[] receivedBytes = receivePacket.getData();
            processData(receivedBytes, clientAddress, clientPort, serverSocket);
        }
        //once server is stopped
        serverSocket.leaveGroup(mcIPAddress);
        serverSocket.close();
    }

    private void startStandardServer() throws IOException {
        DatagramSocket serverSocket = new DatagramSocket(this.port);
        System.out.println(this.trackerInstance.getTrackerId() + "\tStandard UDP server running at:" + serverSocket.getLocalSocketAddress());
        //buffer
        byte[] receiveData = new byte[1024];
        while (keepServerAlive) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            //get es.deusto.ssdd.client address for response
            InetAddress clientAddress;
            clientAddress = receivePacket.getAddress();
            int clientPort = receivePacket.getPort();
            //read received data as bytearray
            byte[] receivedBytes = receivePacket.getData();
            processData(receivedBytes, clientAddress, clientPort, serverSocket);
        }
        //once server is stopped
        serverSocket.close();
    }

    private void processData(byte[] receivedBytes, InetAddress clientAddress, int clientPort, DatagramSocket serverSocket) throws IOException {
        //parse data
        if (this.trackerInstance.isMaster()) {
            byte[] response = parseData(clientAddress, clientPort, receivedBytes);
            //send response
            if (response != null && response.length > 0) {
                DatagramPacket sendPacket = new DatagramPacket(response, response.length, clientAddress, clientPort);
                this.trackerInstance.addLogLine("Debug: Sending message to es.deusto.ssdd.client " + clientAddress.getHostAddress() + " on port " + clientPort);
                serverSocket.send(sendPacket);
            }
        }
    }

    private byte[] parseData(InetAddress clientAddress, int clientPort, byte[] receivedBytes) {
        //first, deserialize data
        try {
            ByteBuffer buffer = this.deserialize(receivedBytes);
            if (buffer != null) {
                //step 2: convert generic buffer to proper object type. get action id
                int value = buffer.getInt(8);
                //parse byte array depending on its action id value
                BitTorrentUDPRequestMessage parsedRequestMessage = PeerRequestParser.parse(value, receivedBytes);
                if (parsedRequestMessage != null) {
                    //validate received message
                    boolean valid = PeerRequestParser.validate(this, value, parsedRequestMessage);
                    if (valid) {
                        PeerRequestParser.triggerOnReceiveEvent(this, value, clientAddress, clientPort, parsedRequestMessage);
                        //valid message. response
                        return PeerRequestParser.getResponse(this, value, parsedRequestMessage);
                    } else {
                        this.trackerInstance.addLogLine("Error: Invalid message detected of type " + parsedRequestMessage.getClass().getSimpleName());
                        return PeerRequestParser.getError(value, false, parsedRequestMessage, "invalid message");
                    }
                } else {
                    this.trackerInstance.addLogLine("Error: NULL message detected ");
                    return PeerRequestParser.getError(value, false, null, "Malformed message");
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println(e.getLocalizedMessage());
        }
        return null;
    }

    private ByteBuffer deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.BIG_ENDIAN);
        return buffer;
    }

    public int getListeningPort() {
        return port;
    }

    public void stopService() {
        //stop the main listener thread
        //close the socket
        serverSocket.close();
        this.trackerInstance.addLogLine("Debug: UDP Multicast server stopped");
    }

    public void savePeerIdForValidation(long randomID) {
        this.connectionIdList.put(randomID, System.currentTimeMillis());
    }

    public boolean isConnectionIdStillValid(long id) {
        Long firstSeenAt = this.connectionIdList.get(id);
        if (firstSeenAt != null) {
            //get current time
            long currentTime = System.currentTimeMillis();
            //calculate difference
            long diff = Math.abs(firstSeenAt - currentTime);
            return diff <= TWO_MINUTES_MILLIS;
        }
        return false;
    }

    public String getTrackerId() {
        return this.trackerInstance.getTrackerId();
    }

    public TrackerInstance getTracker() {
        return this.trackerInstance;
    }

    public void addLogLine(String data) {
        this.trackerInstance.addLogLine(data);
    }

    public void addPeerToSwarm(InetAddress clientAddress, int clientPort, AnnounceRequest request) {
        this.trackerInstance.addLogLine("Adding PEER to swarm...");
        this.trackerInstance.addPeerToSwarm(
                request.getHexInfoHash(),
                clientAddress.getHostAddress(),
                clientPort
        );
    }

    public List<ScrapeInfo> findSwarmInfo(List<String> infoHashes) {
        return this.trackerInstance.findSwarmInfo(infoHashes);
    }
}
