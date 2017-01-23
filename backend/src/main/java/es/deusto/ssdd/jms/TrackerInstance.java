package es.deusto.ssdd.jms;

import bittorrent.udp.PeerInfo;
import bittorrent.udp.ScrapeInfo;
import es.deusto.ssdd.bittorrent.core.SwarmInfo;
import es.deusto.ssdd.bittorrent.core.TrackerUtil;
import es.deusto.ssdd.bittorrent.persistent.ConsensusManager;
import es.deusto.ssdd.bittorrent.persistent.PersistenceHandler;
import es.deusto.ssdd.bittorrent.persistent.SwarmData;
import es.deusto.ssdd.client.udp.model.SharingFile;
import es.deusto.ssdd.gui.model.observ.TorrentObservable;
import es.deusto.ssdd.gui.model.observ.TorrentObserver;
import es.deusto.ssdd.gui.view.TrackerWindow;
import es.deusto.ssdd.jms.listener.JMSMessageListener;
import es.deusto.ssdd.jms.listener.JMSMessageSender;
import es.deusto.ssdd.jms.listener.KeepAliveDaemon;
import es.deusto.ssdd.jms.message.MessageCollection;
import es.deusto.ssdd.jms.model.TrackerDaemonSpec;
import es.deusto.ssdd.jms.model.TrackerInstanceNodeType;
import es.deusto.ssdd.jms.model.TrackerStatus;
import es.deusto.ssdd.udp.TrackerUDPServer;

import javax.jms.JMSException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static es.deusto.ssdd.jms.model.TrackerDaemonSpec.*;

public class TrackerInstance implements Comparable, TorrentObservable {

    //constants
    private static final String ACTIVE_MQ_SERVER = "tcp://localhost:61616";
    private static final int THIS_IS_OLDER = 1;
    private static final int TIMESTAMP_VALUE = 1;
    private static final String ID_SEPARATOR_TAG = "_";
    private static final ConcurrentHashMap<String, TrackerInstance> map = new ConcurrentHashMap<>();
    private static final int MAX_KEEP_ALIVE_TIME = 5;

    // secure concurrent modification allowed variable
    private volatile static AtomicInteger counter = new AtomicInteger(0);
    private static boolean localDeployed;
    //tracker node list
    private final HashMap<String, TrackerInstance> trackerNodeList;
    private String trackerId;
    //instance attributes
    private String ip;
    private int port;
    private TrackerStatus trackerStatus;
    private boolean nodeAlive;
    private AtomicInteger pendingLifetime;
    //node type
    private TrackerInstanceNodeType nodeType;
    //master node
    private TrackerInstance masterNode;
    //KeepAlive daemon
    private KeepAliveDaemon keepaliveDaemon;
    //tracker instance window
    private TrackerWindow trackerWindow;
    //tracker udp server
    private TrackerUDPServer udpServer;
    private PersistenceHandler persistenceHandler;
    //listener map
    private HashMap<TrackerDaemonSpec, JMSMessageListener> listenerHashMap;
    //sender map
    private HashMap<TrackerDaemonSpec, JMSMessageSender> senderHashMap;
    //list of peers sharing a given file
    private SwarmData peerMap;
    private ArrayList<TorrentObserver> observerList;
    private boolean connectedToJMS;
    private Thread keepAliveThread;

    //consensus manager for persistance handling
    private ConsensusManager consensusManager;

    public TrackerInstance() {
        this.observerList = new ArrayList<>();

        //build consensus manager
        this.consensusManager = new ConsensusManager(this);
        this.consensusManager.startOnBackground();

        //show tracker window
        showTrackerWindow();

        this.trackerStatus = TrackerStatus.OFFLINE;

        addLogLine("Running tracker instance " + (counter.incrementAndGet()));
        setTrackerId(generateId());
        setIp(TrackerUtil.getIP());
        addLogLine("Tracker ID: " + trackerId);

        //add to instance map. only for development with multinodes in local mode
        TrackerInstance.map.put(trackerId, this);

        //init tracker node list
        trackerNodeList = new HashMap<>();

        //init peer map
        this.setPeerMap(new SwarmData());

        setupDaemons();

        //add itself to tracker node list
        trackerNodeList.put(this.getTrackerId(), this);

        //init master node
        masterNode = null;

        //init node type
        setNodeType(TrackerInstanceNodeType.MASTER);
        setPendingLifetime(new AtomicInteger(MAX_KEEP_ALIVE_TIME));

        //init persistenceHandler
        persistenceHandler = new PersistenceHandler(this);
        addObserver(persistenceHandler);
    }

    public static TrackerInstance getNode(String id) {
        return TrackerInstance.map.get(id);
    }

    public void addLogLine(String data) {
        if (this.trackerWindow != null) {
            this.trackerWindow.addLogLine(data);
        }
    }

    private void deployUDP() throws IOException {
        udpServer = new TrackerUDPServer(this, ip);
        udpServer.backgroundDispatch();
        this.setPort(udpServer.getListeningPort());
    }

    private synchronized String generateId() {
        return
                TrackerUtil.getDeviceMacAddress()
                        + ID_SEPARATOR_TAG
                        + System.nanoTime();
    }

    public AtomicInteger getPendingLifetime() {
        return pendingLifetime;
    }

    public void setPendingLifetime(AtomicInteger pendingLifetime) {
        this.pendingLifetime = pendingLifetime;
        notifyObserver();
    }

    public boolean getNodeAlive() {
        return nodeAlive;
    }

    public void setNodeAlive(boolean nodeAlive) {
        this.nodeAlive = nodeAlive;
        notifyObserver();
    }

    private synchronized void deployServices() {

        //start handshake service actors
        thread(getListener(HANDSHAKE_SERVICE), false);
        thread(getSender(HANDSHAKE_SERVICE), false);

        //start keep alive service actors
        thread(getListener(KEEP_ALIVE_SERVICE), false);
        thread(getSender(KEEP_ALIVE_SERVICE), false);

        //start data sync service actors
        thread(getListener(DATA_SYNC_SERVICE), false);
        thread(getSender(DATA_SYNC_SERVICE), false);
    }

    private void setupDaemons() {
        //init maps
        senderHashMap = new HashMap<>();
        listenerHashMap = new HashMap<>();

        setupListenerDaemons();
        setupSenderDaemons();
    }

    private void setupSenderDaemons() {
        //populate maps
        try {
            senderHashMap.put(HANDSHAKE_SERVICE,
                    new JMSMessageSender(this, ACTIVE_MQ_SERVER, HANDSHAKE_SERVICE)
            );
        } catch (JMSException e) {
            addLogLine("Error: " + e.getLocalizedMessage());
        }
        try {
            senderHashMap.put(KEEP_ALIVE_SERVICE,
                    new JMSMessageSender(this, ACTIVE_MQ_SERVER, KEEP_ALIVE_SERVICE)
            );
        } catch (JMSException e) {
            addLogLine("Error: " + e.getLocalizedMessage());
        }
        try {
            senderHashMap.put(DATA_SYNC_SERVICE,
                    new JMSMessageSender(this, ACTIVE_MQ_SERVER, DATA_SYNC_SERVICE)
            );
        } catch (JMSException e) {
            addLogLine("Error: " + e.getLocalizedMessage());
        }
    }

    private void setupListenerDaemons() {
        listenerHashMap.put(HANDSHAKE_SERVICE,
                new JMSMessageListener(trackerId, ACTIVE_MQ_SERVER, HANDSHAKE_SERVICE)
        );
        listenerHashMap.put(KEEP_ALIVE_SERVICE,
                new JMSMessageListener(trackerId, ACTIVE_MQ_SERVER, KEEP_ALIVE_SERVICE)
        );
        listenerHashMap.put(DATA_SYNC_SERVICE,
                new JMSMessageListener(trackerId, ACTIVE_MQ_SERVER, DATA_SYNC_SERVICE)
        );
    }

    private void showTrackerWindow() {
        trackerWindow = new TrackerWindow(getCurrentTrackerInstance());
        trackerWindow.setVisible(true);
        addObserver(trackerWindow);
        addObserver(trackerWindow.getLogWindow());
    }

    private TrackerInstance getCurrentTrackerInstance() {
        return this;
    }

    public void beginMasterElectionProcess() {
        addLogLine(trackerId + " Master election process begin");
        beginElections();
    }

    private void beginElections() {
        if (notEnoughNodesForVotation()) {
            addLogLine(trackerId + " setting as local MASTER");
            nodeType = TrackerInstanceNodeType.MASTER;
        } else {
            this.masterNode = getOlderTrackerInstance();
            addLogLine(trackerId + " Cluster MASTER node is: " + masterNode.getTrackerId());
            updateSelfNodeType();
        }
        notifyObserver();
    }

    private TrackerInstance getOlderTrackerInstance() {
        Map<String, TrackerInstance> copiedTrackerList = getTrackerNodeList();
        Iterator it = copiedTrackerList.entrySet().iterator();
        TrackerInstance olderIdInstance = this;
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            TrackerInstance instance = (TrackerInstance) pair.getValue();
            olderIdInstance = this.getOlderTrackerNode(olderIdInstance, instance);
            it.remove();
        }
        return olderIdInstance;
    }

    private boolean notEnoughNodesForVotation() {
        return trackerNodeList.size() == 1;
    }

    private void updateSelfNodeType() {
        if (this.equals(masterNode)) {
            this.nodeType = TrackerInstanceNodeType.MASTER;
        } else {
            this.nodeType = TrackerInstanceNodeType.SLAVE;
        }
        notifyObserver();
    }

    public void _debug_election_result() {
        for (Object o : getTrackerNodeList().entrySet()) {
            Map.Entry pair = (Map.Entry) o;
            TrackerInstance instance = (TrackerInstance) pair.getValue();
            addLogLine("\tDEBUG: " + this.trackerId + " knows that " + instance.trackerId + " is now " + instance.getNodeType());
        }
        addLogLine(trackerId + " Cluster MASTER node is: " + masterNode.getTrackerId());
    }

    private TrackerInstance getOlderTrackerNode(TrackerInstance olderIdInstance, TrackerInstance instance) {
        int result = olderIdInstance.compareTo(instance);
        if (result == 0) {
            return olderIdInstance;
        } else if (result >= 1) {
            return updateNodePairTypes(instance, olderIdInstance);
        } else {
            return updateNodePairTypes(olderIdInstance, instance);
        }
    }

    private TrackerInstance updateNodePairTypes(TrackerInstance olderIdInstance, TrackerInstance instance) {
        olderIdInstance.setNodeType(TrackerInstanceNodeType.MASTER);
        instance.setNodeType(TrackerInstanceNodeType.SLAVE);
        return olderIdInstance;
    }

    private synchronized Thread thread(Runnable runnable, boolean daemon) {
        Thread brokerThread = new Thread(runnable);
        brokerThread.setDaemon(daemon);
        brokerThread.start();
        return brokerThread;
    }

    public synchronized void deploy() throws JMSException {

        //deploy background udp server
        try {
            deployUDP();
        } catch (IOException e) {
            addLogLine("error: could not deployUDP server");
            addLogLine("error: caused by "+e.getLocalizedMessage());
        }

        //check if localhost active mq is active. otherwise, execute .bat
        if (!localDeployed) {
            autoDeployActiveMq();
        }

        //deploy our background services
        deployServices();

        //init keep alive daemon
        startkeepAlive();

        //send first hello world message for tracker master detection
        MessageCollection message = MessageCollection.HELLO_WORLD;
        getSender(HANDSHAKE_SERVICE).send(message);
    }

    private void startkeepAlive() {
        keepaliveDaemon = new KeepAliveDaemon(this);
        keepAliveThread = new Thread(keepaliveDaemon);
        keepAliveThread.start();
        notifyObserver();
    }

    private void autoDeployActiveMq() {
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            deployActiveMQCommand("cmd /c start C:\\Development\\Tools\\apache-activemq-5.14.1\\bin\\win64\\activemq.bat");
        } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            deployActiveMQCommand("/Volumes/HDD/dev/activemq/bin/activemq.sh");
        } else if (System.getProperty("os.name").toLowerCase().contains("nix")) {
            deployActiveMQCommand("/Volumes/HDD/dev/activemq/bin/activemq.sh");
        }
    }

    private void deployActiveMQCommand(String cmd) {
        try {
            Runtime.getRuntime().exec(cmd);
            //wait 10 second (average) until full deploy
            this.addLogLine("debug: Waiting 10 s to Active MQ to deploy...");
            Thread.sleep(10000);
            localDeployed = true;
        } catch (IOException | InterruptedException e) {
            addLogLine("error: deploying Active MQ");
            addLogLine("error: caused by "+e.getLocalizedMessage());
        }
    }

    public JMSMessageSender getSender(TrackerDaemonSpec spec) {
        return senderHashMap.get(spec);
    }

    private JMSMessageListener getListener(TrackerDaemonSpec spec) {
        return listenerHashMap.get(spec);
    }

    public String getTrackerId() {
        return trackerId;
    }

    public void setTrackerId(String trackerId) {
        this.trackerId = trackerId;
        notifyObserver();
    }

    public TrackerInstanceNodeType getNodeType() {
        return nodeType;
    }

    private void setNodeType(TrackerInstanceNodeType nodeType) {
        this.nodeType = nodeType;
        notifyObserver();
    }

    public HashMap<String, TrackerInstance> getTrackerNodeList() {
        if (trackerNodeList == null) {
            return null;
        }
        return (HashMap<String, TrackerInstance>) trackerNodeList.clone();
    }

    public void addRemoteNode(TrackerInstance remoteNode) {
        this.trackerNodeList.put(remoteNode.getTrackerId(), remoteNode);
        notifyObserver();
    }

    public void removeRemoteNode(String sourceTrackerId) {
        TrackerInstance node = TrackerInstance.getNode(sourceTrackerId);
        addLogLine(trackerId + " Removing remote node " + node + "from active trackers list");
        if (node != null) {
            removeNodeFromList(node);
            removeMasterNodeStatus(node);
            notifyObserver();
        }
    }

    private void removeMasterNodeStatus(TrackerInstance node) {
        //eliminar el master en caso de que coincida
        if (node.equals(masterNode)) {
            masterNode = null;
        }
        notifyObserver();
    }

    private boolean removeNodeFromList(TrackerInstance node) {
        TrackerInstance removed = this.trackerNodeList.remove(node.getTrackerId());
        boolean success = removed != null;
        if (success) {
            addLogLine(trackerId + " Successfully removed node " + node.getTrackerId());
            notifyObserver();
        } else {
            addLogLine(trackerId + " Failed removing node " + node.getTrackerId());
        }
        return success;
    }

    @Override
    public int compareTo(Object o) {
        if (o.getClass().equals(TrackerInstance.class)) {
            TrackerInstance i = (TrackerInstance) o;
            return this.getTrackerTimeStamp().compareTo(i.getTrackerTimeStamp());
        }
        return THIS_IS_OLDER;
    }

    private String getTrackerTimeStamp() {
        String[] data = getTrackerId().split(ID_SEPARATOR_TAG);
        return data[TIMESTAMP_VALUE];
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
        notifyObserver();
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
        notifyObserver();
    }

    public TrackerStatus getTrackerStatus() {
        return trackerStatus;
    }

    private void setTrackerStatus(TrackerStatus trackerStatus) {
        this.trackerStatus = trackerStatus;
        notifyObserver();
    }

    public boolean isMaster() {
        return nodeType == TrackerInstanceNodeType.MASTER;
    }

    public void sayGoodByeToCluster() {
        try {
            JMSMessageSender sender = this.getSender(HANDSHAKE_SERVICE);
            if (sender != null) {
                sender.send(MessageCollection.BYE_BYE);
            }
            this.setTrackerStatus(TrackerStatus.OFFLINE);
            notifyObserver();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    private void stopSendingKeepAlives() {
        //stop sending keep alives
        this.nodeAlive = false;
        notifyObserver();
    }

    public boolean isAlreadyDiscovered(TrackerInstance node) {
        return trackerNodeList.get(node.getTrackerId()) != null;
    }

    public void resetRemoteNodeTimeInLocalRegistry(TrackerInstance remoteNode) {
        addLogLine(this.getTrackerId() + " resetting " + remoteNode.getTrackerId() + " KEEP_ALIVE value");
        this.pendingLifetime.set(MAX_KEEP_ALIVE_TIME);
        notifyObserver();
    }

    public String toString() {
        return nodeType + "::" + getTrackerTimeStamp();
    }

    public boolean hasRollAsigned() {
        return nodeType != null;
    }

    public boolean isMasterKnown() {
        return masterNode != null;
    }

    public int getLastKeepAlive() {
        return pendingLifetime.get();
    }

    public void decreasePendingLifeTime() {
        this.pendingLifetime.decrementAndGet();
        notifyObserver();
    }

    public void removeDeadNodesFromList(ArrayList<TrackerInstance> instancesToRemove) {
        instancesToRemove.forEach(this.trackerNodeList::remove);
        notifyObserver();
    }

    public boolean isKeepAliveCountDead() {
        return pendingLifetime.get() == 0;
    }

    public ArrayList<TrackerInstance> getTrackerNodesAsList() {
        ArrayList<TrackerInstance> nodes = new ArrayList<>();
        for (Object o : getTrackerNodeList().entrySet()) {
            Map.Entry pair = (Map.Entry) o;
            TrackerInstance instance = (TrackerInstance) pair.getValue();
            nodes.add(instance);
        }
        return nodes;
    }

    public boolean isAlive() {
        return this.nodeAlive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TrackerInstance that = (TrackerInstance) o;
        return trackerId.equals(that.trackerId);
    }

    private String getTrackerDatabaseName() {
        return this.persistenceHandler.getDatabaseName();
    }

    //send current database to remote node
    public void sendDatabaseBack(TrackerInstance remoteNode) {
        try {
            MessageCollection message = MessageCollection.DATABASE_CLONE;
            message.setRemoteNode(remoteNode);
            this.getSender(TrackerDaemonSpec.DATA_SYNC_SERVICE)
                    .send(message);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void overwriteLocalDatabase(byte[] binaryContent) {
        this.persistenceHandler.overwrite(binaryContent);
    }

    public void stopNode() {
        //stop sending keep alives
        stopSendingKeepAlives();
        //stop UDP server
        udpServer.stopService();
        //delete local database
        persistenceHandler.deleteDatabase();
        notifyObserver();
    }

    public void syncData(String data) {
        addLogLine(trackerId + " Synchronization received");
        this.persistenceHandler.saveData(data);
    }

    public SwarmInfo findAnnounceInfoOf(String hexInfoHash) {
        addLogLine(this.trackerId + "\tFinding torrent metainfo about " + hexInfoHash);
        if (hexInfoHash != null) {
            return this.getPeerMap().get(hexInfoHash);
        }
        return null;
    }

    @Override
    public void addObserver(TorrentObserver o) {
        this.observerList.add(o);
    }

    @Override
    public void removeObserver(TorrentObserver o) {
        this.observerList.remove(o);
    }

    @Override
    public void notifyObserver() {
        for (TorrentObserver o : observerList) {
            o.update();
        }
    }

    public boolean isConnectedToJMS() {
        return connectedToJMS;
    }

    public void setConnectedToJMS(boolean connectedToJMS) {
        this.connectedToJMS = connectedToJMS;
        this.setNodeAlive(connectedToJMS);
        if (connectedToJMS) {
            //re-lauch keep alive sender
            if (keepAliveThread.getState() == Thread.State.TERMINATED) {
                startkeepAlive();
            }
            this.setTrackerStatus(TrackerStatus.ONLINE);
        } else {
            this.setTrackerStatus(TrackerStatus.OFFLINE);
        }
        this.notifyObserver();
    }

    public int getClientCount() {
        int total = 0;
        if (getPeerMap() != null) {
            for (SwarmInfo si : getPeerMap().values()) {
                List<PeerInfo> list = si.getPeers();
                if (list != null) {
                    total += list.size();
                }
            }
        }
        return total;
    }

    public long getSharingBytesCount() {
        long total = 0;
        if (getPeerMap() != null) {
            for (SwarmInfo si : getPeerMap().values()) {
                SharingFile file = si.getFile();
                if (file != null) {
                    total += file.getTotalBytes();
                }
            }
        }
        return total;
    }

    public int getSwarmCount() {
        if (getPeerMap() != null) {
            return this.getPeerMap().size();
        }
        return 0;
    }

    public void addPeerToSwarm(String hexInfoHash, String hostAddress, int clientPort) {
        SwarmInfo swarm = this.getPeerMap().get(hexInfoHash);
        if (swarm == null) {
            this.addLogLine("debug: building a new swarm for " + hexInfoHash);
            swarm = new SwarmInfo(this);
        }
        this.addLogLine("debug: adding peer (" + hostAddress + ", " + clientPort + ") to " + hexInfoHash + " swarm");
        PeerInfo peerInfo = new PeerInfo();
        peerInfo.setPort(clientPort);
        swarm.addPeer(peerInfo);
        //increase by +1 the number of leecher since this method is called when a new announce request is received
        swarm.increaseLeechersBy(1);
        this.getPeerMap().put(hexInfoHash, swarm);
        //save this peer info on db
        notifyObserver();
    }

    public PersistenceHandler getPersistenceHandler() {
        return persistenceHandler;
    }

    public List<ScrapeInfo> findSwarmInfo(List<String> infoHashes) {
        if(infoHashes!=null){
            List<ScrapeInfo> info = new ArrayList<>();
            for (String hash : infoHashes){
                ScrapeInfo scrapeInfo = this.findScrapeInfoOf(hash);
                if(scrapeInfo!=null){
                    info.add(scrapeInfo);
                }
            }
            return info;
        }
        return null;
    }

    private ScrapeInfo findScrapeInfoOf(String hash) {
        if(hash!=null){
            SwarmInfo data = this.getPeerMap().get(hash);
            if(data!=null){
                ScrapeInfo scrapeData = new ScrapeInfo();
                scrapeData.setCompleted(data.getTimesDownloaded());
                scrapeData.setLeechers(data.getLeechers());
                scrapeData.setSeeders(data.getSeeders());
                return scrapeData;
            }
        }
        return null;
    }

    public SwarmData getSwarmInfo() {
        return this.getPeerMap();
    }

    public byte[] getDatabaseInfoAsArray() {
        return this.persistenceHandler.getDatabaseInfoAsArray();
    }

    public void notifyDatabaseHasChanged() {
        //send first hello world message for tracker master detection
        try {
            MessageCollection message = MessageCollection.SYNC;
            getSender(DATA_SYNC_SERVICE).send(message);
        } catch (JMSException e) {
            addLogLine("error: could not send database consensus request");
            addLogLine("error: caused by "+e.getLocalizedMessage());
        }
    }

    public boolean canSaveData() {
        return this.persistenceHandler.canSaveData();
    }

    public SwarmData getPeerMap() {
        return peerMap;
    }

    public void setPeerMap(SwarmData peerMap) {
        this.peerMap = peerMap;
    }
}