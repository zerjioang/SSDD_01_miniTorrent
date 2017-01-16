package es.deusto.ssdd.client.udp.client;

import bittorrent.udp.*;
import es.deusto.ssdd.client.udp.model.SharingFile;
import es.deusto.ssdd.client.udp.model.TrackerResponseParser;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by .local on 15/01/2017.
 */
public class PeerClient {

    private static final int MIN_PORT = 1000;
    private static final int MAX_PORT = 5000;
    private static final long ID_UPDATE_FREQ_MS = 60 * 1000; //1 minute

    private ArrayList<SharingFile> sharingFiles;
    private final String ip;
    private final int port;
    private boolean keepListening;
    private boolean needToConnect;
    private boolean needToAnnounce;
    //keep connection response for saving connection id
    private ConnectResponse connectionResponse;
    private int peerId;
    private InetAddress serverHost;
    private DatagramSocket clientSocket;

    public PeerClient(String ip, int port) {
        this.ip = ip;
        this.port = port;
        this.keepListening = true;
        this.needToConnect = true;
        this.needToAnnounce = true;
        this.sharingFiles = new ArrayList<SharingFile>();
        this.peerId = -1;
        //this.port = MIN_PORT + (int) (Math.random() * ((MAX_PORT - MIN_PORT) + 1));
    }

    public void connect() throws IOException {

        //run task for id update every 1 minute
        updateIdTask();

        System.out.println("PEER CLIENT: Running demo udp client");
        clientSocket = new DatagramSocket();
        serverHost = InetAddress.getByName(ip);

        //listen for new messages
        byte[] receiveData = new byte[1024];
        while(keepListening){
            if(needToConnect){
                connectionRequest(clientSocket, serverHost);
            }
            else if(needToAnnounce){
                for(SharingFile file : sharingFiles){
                    //realizar una solicitud de obtencion de listado de peers por cada fichero que se quiere descargar
                    announceRequest(file, clientSocket, serverHost, AnnounceRequest.Event.NONE);
                }
                //todo no esta bien del todo ya que o se envian todos de golpe o bien o nada
                //si falla uno, no se vuelve a enviar
                needToAnnounce = false;
            }
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            clientSocket.receive(receivePacket);
            byte[] receivedData = receivePacket.getData();
            byte[] response = parseData(receivedData);
        }
        //close connection. free resources
        clientSocket.close();
    }

    private void updateIdTask() {
        Timer timer = new Timer();
        timer.schedule(this.updateId(), 0, ID_UPDATE_FREQ_MS);
    }

    private TimerTask updateId() {
        return new TimerTask() {
            @Override
            public void run() {
                //ejecutar contenido del timer solo si ya se ha obtenido al menos el primer id, desde el servidor
                if(getThisPeerClient().peerId!=-1){
                    System.out.println("PEER CLIENT: New peer id needed: requesting...");
                    getThisPeerClient().setNeedToConnect(true);
                    try {
                        connectionRequest(clientSocket, serverHost);
                    } catch (IOException e) {
                        System.err.println("PEER CLIENT: IO Exception when requesting new ID");
                    }
                }
            }
        };
    }

    public PeerClient getThisPeerClient(){
        return this;
    }

    private void connectionRequest(DatagramSocket clientSocket, InetAddress serverHost) throws IOException {
        //El Peer envía al Tracker el mensaje “connect request"
        ConnectRequest request = new ConnectRequest();
        //update my id and notify to tracker
        int newId = getRandomID();
        request.setTransactionId(newId);
        //send
        sendPacket(clientSocket, serverHost, request);
    }

    private void announceRequest(SharingFile file, DatagramSocket clientSocket, InetAddress serverHost, AnnounceRequest.Event requestedEvent) throws IOException {

        //El Peer envía al Tracker el mensaje “announce request"
        AnnounceRequest request = new AnnounceRequest();
        request.setConnectionId(this.getConnectionId());
        request.setAction(BitTorrentUDPMessage.Action.ANNOUNCE);
        request.setTransactionId(this.getTransactionId());
        //infohash sobre el fichero a obtener la informacion
        //TODO leer infohash
        request.setInfoHash(file.getInfohash());
        //peer id as string
        request.setPeerId(String.valueOf(this.peerId));
        //downloaded
        request.setDownloaded(file.getDownloadedBytes());
        //left
        request.setLeft(file.getLeftBytes());
        //uploaded
        request.setUploaded(file.getUploadedBytes());
        //event
        request.setEvent(requestedEvent);
        //ip_adress
        PeerInfo info = new PeerInfo();
        info.setIpAddress(0);
        info.setPort(this.port);
        request.setPeerInfo(info); // 0 (poniendo 0 el Tracker usará la IP de origen del paquete UDP).
        //key (random)
        request.setKey(getRandomID());
        //num_want (default: -1) o el numero maximo de peers que se quiere recibir
        request.setNumWant(-1);

        //send built message
        sendPacket(clientSocket, serverHost, request);
    }

    private byte[] parseData(byte[] receivedBytes) {
        //first, deserialize data
        try {
            ByteBuffer buffer = this.deserialize(receivedBytes);
            if(buffer!=null){
                //step 2: convert generic buffer to proper object type. get action id
                int value = buffer.getInt(8);
                //parse byte array depending on its action id value
                BitTorrentUDPMessage parsedRequestMessage = TrackerResponseParser.parse(value, receivedBytes);
                //validate received message
                boolean valid = TrackerResponseParser.validate(value, parsedRequestMessage);
                if(valid){
                    TrackerResponseParser.triggerOnReceiveEvent(this, value, parsedRequestMessage);
                    //valid message. response
                    return TrackerResponseParser.getRequest(value, parsedRequestMessage);
                }
                else{
                    System.err.println("PEER CLIENT: Invalid message detected of type "+parsedRequestMessage.getClass().getSimpleName());
                    return TrackerResponseParser.getError(value, valid, parsedRequestMessage);
                }
            }
        } catch (IOException e) {
            System.err.println(e.getLocalizedMessage());
        } catch (ClassNotFoundException e) {
            System.err.println(e.getLocalizedMessage());
        }
        return null;
    }

    private void sendPacket(DatagramSocket clientSocket, InetAddress serverHost, BitTorrentUDPRequestMessage request) throws IOException {
        byte[] requestBytes = request.getBytes();
        DatagramPacket packet = new DatagramPacket(requestBytes, requestBytes.length, serverHost, port);
        System.out.println("PEER CLIENT: Sending "+request.getClass().getSimpleName()+" packet to "+ip+" on port "+port);
        clientSocket.send(packet);
    }

    public static final int getRandomID() {
        //El primer paso para comenzar la comunicación es la obtención de un ID de conexión (connection_id). Este ID se utiliza para garantizar el Peer es quién dice ser y no es suplantado. El ID debe renovarse periódicamente.
        Random random = new Random();
        return random.nextInt(Integer.MAX_VALUE);
    }

    private ByteBuffer deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.BIG_ENDIAN);
        return buffer;
    }

    //getters and setters

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public boolean isKeepListening() {
        return keepListening;
    }

    public void setKeepListening(boolean keepListening) {
        this.keepListening = keepListening;
    }

    public boolean isNeedToConnect() {
        return needToConnect;
    }

    public void setNeedToConnect(boolean needToConnect) {
        this.needToConnect = needToConnect;
    }

    public boolean isNeedToAnnounce() {
        return needToAnnounce;
    }

    public void setNeedToAnnounce(boolean needToAnnounce) {
        this.needToAnnounce = needToAnnounce;
    }

    public long getConnectionId() {
        return getConnectionResponse().getConnectionId();
    }

    public int getTransactionId() {
        return getConnectionResponse().getTransactionId();
    }

    public void setConnectionResponse(ConnectResponse connectionResponse) {
        this.connectionResponse = connectionResponse;
    }

    public ConnectResponse getConnectionResponse() {
        return connectionResponse;
    }

    public void shareFile(SharingFile file) {
        if(file!=null){
            this.sharingFiles.add(file);
        }
    }

    public void setPeerId(int peerId) {
        this.peerId = peerId;
    }
}
