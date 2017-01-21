package es.deusto.ssdd.client.udp.client;

import bittorrent.metainfo.InfoDictionarySingleFile;
import bittorrent.metainfo.MetainfoFile;
import bittorrent.metainfo.handler.MetainfoHandlerSingleFile;
import bittorrent.udp.*;
import es.deusto.ssdd.client.udp.model.SharingFile;
import es.deusto.ssdd.client.udp.model.TrackerResponseParser;

import java.io.File;
import java.io.IOException;
import java.net.*;
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

    private static final long ID_UPDATE_FREQ_MS = 60 * 1000; //1 minute
    private static final long UPDATE_ANNOUNCE_STATUS_FREQ_MS = 10 * 1000; //10 s
    private static final String MULTICAST_IP = "224.0.0.1";
    private static final int MULTICAST_PORT = 1234;

    private ArrayList<SharingFile> sharingFiles;
    private String ip;
    private int port;
    private boolean keepListening;
    private boolean needToConnect;
    private boolean needToAnnounce;
    //keep connection response for saving connection id
    private ConnectResponse connectionResponse;
    private int peerId;
    private InetAddress serverHost;
    private DatagramSocket clientSocket;
    private boolean multicastEnabled;

    public PeerClient() {
        this.keepListening = true;
        this.needToConnect = true;
        this.needToAnnounce = true;
        this.sharingFiles = new ArrayList<>();
        this.peerId = -1;
        this.multicastEnabled = true;
        this.port = MULTICAST_PORT;
    }

    public PeerClient(String ip, int port) {
        this();
        this.ip = ip;
        this.port = port;
    }

    public static final int getRandomID() {
        //El primer paso para comenzar la comunicación es la obtención de un ID de conexión (connection_id). Este ID se utiliza para garantizar el Peer es quién dice ser y no es suplantado. El ID debe renovarse periódicamente.
        Random random = new Random();
        return random.nextInt(Integer.MAX_VALUE);
    }

    public void connect() throws IOException {
        new Thread(() -> {
            System.out.println("PEER CLIENT: Running udp client");
            //run task for id update every 1 minute
            updateClientIDTimer();
            try {
                createSocket();
                //get server ip
                setRemoteHost();
                //listen for new messages
                startListening();
                //close connection. free resources
                close();
            } catch (IOException e) {
                System.err.println(e.getLocalizedMessage());
            }
        }).start();
    }

    private void createSocket() throws SocketException {
        clientSocket = new DatagramSocket();
    }

    private void startListening() throws IOException {
        byte[] receiveData = new byte[1024];
        while(keepListening){
            sendMessageIfNeeded();
            DatagramPacket receivePacket = readIncomingPacket(receiveData);
            byte[] receivedData = receivePacket.getData();
            byte[] response = parseData(receivedData);
            processResponse(response);
        }
    }

    private void sendMessageIfNeeded() throws IOException {
        if (needToConnect) {
            connectionRequest(clientSocket, serverHost);
        }
    }

    private boolean evaluateNeedToAnnounce() {
        boolean announce = false;
        for (SharingFile sh : sharingFiles) {
            announce |= sh.needsToBeAnnounced();
        }
        return announce;
    }

    private void processResponse(byte[] response) {
        if (response != null) {
            //sendPacket(clientSocket, serverHost, response);
        }
    }

    private DatagramPacket readIncomingPacket(byte[] receiveData) throws IOException {
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        clientSocket.receive(receivePacket);
        return receivePacket;
    }

    private void close() {
        clientSocket.close();
    }

    private void setRemoteHost() throws UnknownHostException {
        if (multicastEnabled) {
            serverHost = InetAddress.getByName(MULTICAST_IP);
        } else {
            serverHost = InetAddress.getByName(ip);
        }
    }

    private void updateClientIDTimer() {
        Timer timer = new Timer();
        timer.schedule(this.updateId(), 0, ID_UPDATE_FREQ_MS);
        //execute one announce request timer task for each sharing file
        for (SharingFile f : sharingFiles) {
            timer.schedule(this.updateAnnounceStatus(f), f.getUpdateInterval());
        }
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

    private TimerTask updateAnnounceStatus(SharingFile file) {
        return new TimerTask() {
            @Override
            public void run() {
                //ejecutar contenido solo si ya se tiene una conection
                if (!needToConnect) {
                    if (file == null) {
                        //update announce of all files
                        for (SharingFile f : sharingFiles) {
                            //realizar una solicitud de obtencion de listado de peers por cada fichero que se quiere descargar
                            updateFileAnnounce(f);
                        }
                    } else {
                        updateFileAnnounce(file);
                    }
                    needToAnnounce = evaluateNeedToAnnounce();
                }
            }

            private void updateFileAnnounce(SharingFile file) {
                if (file.needsToBeAnnounced()) {
                    try {
                        announceRequest(file, clientSocket, serverHost, AnnounceRequest.Event.NONE);
                    } catch (IOException e) {
                        System.err.println(e.getLocalizedMessage());
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
            return convertBuffer2Object(receivedBytes, buffer);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println(e.getLocalizedMessage());
        }
        return null;
    }

    private byte[] convertBuffer2Object(byte[] receivedBytes, ByteBuffer buffer) {
        if (buffer != null) {
            //step 2: convert generic buffer to proper object type. get action id
            int value = buffer.get(3);
            //parse byte array depending on its action id value
            BitTorrentUDPMessage parsedRequestMessage = TrackerResponseParser.parse(value, receivedBytes);
            //validate received message
            boolean valid = TrackerResponseParser.validate(value, parsedRequestMessage);
            if (valid) {
                TrackerResponseParser.triggerOnReceiveEvent(this, value, parsedRequestMessage);
                //valid message. response
                return TrackerResponseParser.getRequest(value, parsedRequestMessage);
            } else {
                System.err.println("PEER CLIENT: Invalid message detected of type " + parsedRequestMessage.getClass().getSimpleName());
                return TrackerResponseParser.getError(value, false, parsedRequestMessage);
            }
        }
        return null;
    }

    private void sendPacket(DatagramSocket clientSocket, InetAddress serverHost, BitTorrentUDPRequestMessage request) throws IOException {
        byte[] requestBytes = request.getBytes();
        DatagramPacket packet = new DatagramPacket(requestBytes, requestBytes.length, serverHost, port);
        System.out.println("PEER CLIENT: Sending " + request.getClass().getSimpleName() + " packet to " + serverHost.getHostAddress() + " on port " + port);
        clientSocket.send(packet);
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

    public ConnectResponse getConnectionResponse() {
        return connectionResponse;
    }

    public void setConnectionResponse(ConnectResponse connectionResponse) {
        this.connectionResponse = connectionResponse;
    }

    public void shareFile(SharingFile file) {
        if(file!=null){
            this.sharingFiles.add(file);
        }
    }

    public void setPeerId(int peerId) {
        this.peerId = peerId;
    }

    public SharingFile readTorrent(File torrent) {
        //leer un archivo de la carpeta resources
        MetainfoHandlerSingleFile handler = new MetainfoHandlerSingleFile();
        //read from resouces
        handler.parseTorrenFile(torrent.getAbsolutePath());
        MetainfoFile<InfoDictionarySingleFile> meta = handler.getMetainfo();
        SharingFile sharingFile = new SharingFile(meta.getInfo().getName(), meta.getInfo().getInfoHash(), meta.getInfo().getLength());
        sharingFile.setMetaInfo(meta);
        return sharingFile;
    }

    public void startDownloading(SharingFile shareFile) {
        this.sharingFiles.add(shareFile);
    }

    public void test() throws IOException {
        //leer un archivo de la carpeta resources
        MetainfoHandlerSingleFile handler = new MetainfoHandlerSingleFile();
        //read from resouces
        ClassLoader classLoader = PeerClient.class.getClassLoader();
        File file = new File(classLoader.getResource("torrent/ubuntu-16.04.1-desktop-amd64.iso.torrent").getFile());
        handler.parseTorrenFile(file.getPath());
        MetainfoFile<InfoDictionarySingleFile> meta = handler.getMetainfo();
        SharingFile sharingFile = new SharingFile(meta.getInfo().getName(), meta.getInfo().getInfoHash(), meta.getInfo().getLength());
        sharingFile.setMetaInfo(meta);
        this.shareFile(sharingFile);
        this.connect();
    }
}
