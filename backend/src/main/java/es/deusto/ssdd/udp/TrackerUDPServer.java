package es.deusto.ssdd.udp;

import bittorrent.udp.BitTorrentUDPRequestMessage;
import es.deusto.ssdd.jms.TrackerInstance;
import es.deusto.ssdd.udp.parser.PeerRequestParser;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;

/**
 * Created by .local on 15/12/2016.
 */
public class TrackerUDPServer {

    private static final int MIN_PORT = 1000;
    private static final int MAX_PORT = 5000;
    private static final long TWO_MINUTES_MILLIS = 2 * 60 * 1000;
    private final TrackerInstance trackerInstance;
    private final String ip;
    private int port;
    private ServerSocket welcomeSocket;
    private HashMap<Long, Long> connectionIdList;

    public TrackerUDPServer(TrackerInstance trackerInstance, String ip) {
        this.trackerInstance = trackerInstance;
        this.ip = ip;
        this.port = MIN_PORT + (int) (Math.random() * ((MAX_PORT - MIN_PORT) + 1));
        this.connectionIdList = new HashMap<>();
    }

    public void backgroundDispatch() throws IOException {
        new Thread(() -> {
            System.out.println(trackerInstance.getTrackerId() + " [Starting UDP server on port " + port + "]");
            try {
                startServer();
            } catch (IOException e) {
                System.err.println(e.getLocalizedMessage());
            }
        }).start();
    }

    private void startServer() throws IOException {
        DatagramSocket serverSocket = new DatagramSocket(this.port);
        byte[] receiveData = new byte[1024];
        while(true) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            //get client address for response
            InetAddress clientAddress = receivePacket.getAddress();
            int clientPort = receivePacket.getPort();
            //read received data as bytearray
            byte[] receivedBytes = receivePacket.getData();
            //parse data
            byte[] response = parseData(receivedBytes);
            //send response
            if(response!=null && response.length>0){
                DatagramPacket sendPacket = new DatagramPacket(response, response.length, clientAddress, clientPort);
                serverSocket.send(sendPacket);
            }
        }
    }

    private byte[] parseData(byte[] receivedBytes) {
        //first, deserialize data
        try {
            ByteBuffer buffer = this.deserialize(receivedBytes);
            if(buffer!=null){
                //step 2: convert generic buffer to proper object type. get action id
                int value = buffer.getInt(8);
                //parse byte array depending on its action id value
                BitTorrentUDPRequestMessage parsedRequestMessage = PeerRequestParser.parse(value, receivedBytes);
                if(parsedRequestMessage!=null){
                    //validate received message
                    boolean valid = PeerRequestParser.validate(this, value, parsedRequestMessage);
                    if(valid){
                        PeerRequestParser.triggerOnReceiveEvent(this, value, parsedRequestMessage);
                        //valid message. response
                        return PeerRequestParser.getResponse(this, value, parsedRequestMessage);
                    }
                    else{
                        System.err.println("Invalid message detected of type "+parsedRequestMessage.getClass().getSimpleName());
                        return PeerRequestParser.getError(value, false, parsedRequestMessage);
                    }
                }
                else{
                    System.err.println("NULL message detected ");
                    return PeerRequestParser.getError(value, false, null);
                }
            }
        } catch (IOException e) {
            System.err.println(e.getLocalizedMessage());
        } catch (ClassNotFoundException e) {
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
        try {
            welcomeSocket.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
        System.out.println(trackerInstance.getTrackerId() + " UDP Server stopped");
    }

    public void savePeerIdForValidation(long randomID) {
        this.connectionIdList.put(randomID, System.currentTimeMillis());
    }

    public boolean isIdStillValid(long id){
        Long firstSeenAt = this.connectionIdList.get(id);
        if(firstSeenAt!=null){
            //get current time
            long currentTime = System.currentTimeMillis();
            //calculate difference
            long diff = Math.abs(firstSeenAt.longValue()-currentTime);
            return diff <= TWO_MINUTES_MILLIS;
        }
        return false;
    }
}
