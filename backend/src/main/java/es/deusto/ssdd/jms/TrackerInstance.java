package es.deusto.ssdd.jms;

import es.deusto.ssdd.bittorrent.core.TrackerUtil;
import es.deusto.ssdd.bittorrent.persistent.PersistenceHandler;
import es.deusto.ssdd.gui.view.InterfaceRefresher;
import es.deusto.ssdd.gui.view.TrackerWindow;
import es.deusto.ssdd.jms.listener.JMSMessageListener;
import es.deusto.ssdd.jms.listener.JMSMessageSender;
import es.deusto.ssdd.jms.listener.KeepAliveDaemon;
import es.deusto.ssdd.jms.listener.TrackerDaemonSpec;
import es.deusto.ssdd.jms.message.MessageCollection;
import es.deusto.ssdd.jms.model.TrackerInstanceNodeType;
import es.deusto.ssdd.jms.model.TrackerStatus;

import javax.jms.JMSException;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static es.deusto.ssdd.jms.listener.TrackerDaemonSpec.*;

/**
 * Created by .local on 14/11/2016.
 */
public class TrackerInstance implements Comparable {

    //constants
    private static final String ACTIVE_MQ_SERVER = "tcp://localhost:61616";
    private static final int THIS_IS_OLDER = 1;
    private static final int TIMESTAMP_VALUE = 1;
    private static final String ID_SEPARATOR_TAG = ">";
    private static final ConcurrentHashMap<String, TrackerInstance> map = new ConcurrentHashMap<>();
    private static final int MAX_KEEP_ALIVE_TIME = 5;
    private static final boolean NODE_OFFLINE_MODE = false;
    private static final boolean NODE_ONLINE_MODE = true;

    // secure concurrent modification allowed variable
    private volatile static AtomicInteger counter = new AtomicInteger(0);
    private final String trackerId;
    //tracker node list
    private final HashMap<String, TrackerInstance> trackerNodeList;
    //instance attributes
    private String ip;
    private int port;
    private TrackerStatus trackerStatus;
    private InterfaceRefresher refresh;
    private AtomicBoolean nodeAlive;
    private AtomicInteger pendingLifetime;
    //node type
    private TrackerInstanceNodeType nodeType;

    //master node
    private TrackerInstance masterNode;

    //KeepAlive daemon
    private KeepAliveDaemon keepaliveDaemon;

    //tracker instance window
    private TrackerWindow trackerWindow;

    private PersistenceHandler persistenceHandler;

    //listener map
    private HashMap<TrackerDaemonSpec, JMSMessageListener> listenerHashMap;
    //sender map
    private HashMap<TrackerDaemonSpec, JMSMessageSender> senderHashMap;

    public TrackerInstance() {

        this.trackerStatus = TrackerStatus.OFFLINE;
        synchronized (this) {
            System.out.println("Running tracker instance " + (counter.incrementAndGet()));
            trackerId = generateId();
        }
        ip = TrackerUtil.getIP();
        port = 8000;
        System.out.println("Tracker ID: " + trackerId);

        //add to instance map. only for development with multinodes in local mode
        TrackerInstance.map.put(trackerId, this);

        //init tracker node list
        trackerNodeList = new HashMap<>();

        setupDaemons();

        //add itself to tracker node list
        trackerNodeList.put(this.getTrackerId(), this);

        //init master node
        masterNode = null;

        //init keep alive daemon
        keepaliveDaemon = new KeepAliveDaemon(this);

        //init node type
        nodeType = TrackerInstanceNodeType.MASTER;

        //init keep alive attributes
        nodeAlive = new AtomicBoolean(NODE_ONLINE_MODE);
        pendingLifetime = new AtomicInteger(MAX_KEEP_ALIVE_TIME);

        //init persistenceHandler
        persistenceHandler = new PersistenceHandler(trackerId);

        //deploy our background services
        deployServices();

        //show tracker window
        showTrackerWindow();

        this.trackerStatus = TrackerStatus.ONLINE;
        if (this.refresh != null) {
            this.refresh.updateTrackerStatus(this.trackerStatus);
        }

        //add node itself to window
        updateNodeTable(this.getTrackerNodeList());
    }

    public static TrackerInstance getNode(String id) {
        return TrackerInstance.map.get(id);
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
    }

    public AtomicBoolean getNodeAlive() {
        return nodeAlive;
    }

    public void setNodeAlive(AtomicBoolean nodeAlive) {
        this.nodeAlive = nodeAlive;
    }

    private synchronized void deployServices() {

        //start handshake service actors
        thread(getListener(HANDSHAKE_SERVICE), false);
        thread(getSender(HANDSHAKE_SERVICE), false);

        //start keep alive service actors
        thread(getListener(KEEP_ALIVE_SERVICE), false);
        thread(getSender(KEEP_ALIVE_SERVICE), false);

        //start keep alive daemon
        thread(keepaliveDaemon, false);

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
        senderHashMap.put(HANDSHAKE_SERVICE,
                new JMSMessageSender(trackerId, ACTIVE_MQ_SERVER, HANDSHAKE_SERVICE)
        );
        senderHashMap.put(KEEP_ALIVE_SERVICE,
                new JMSMessageSender(trackerId, ACTIVE_MQ_SERVER, KEEP_ALIVE_SERVICE)
        );
        senderHashMap.put(DATA_SYNC_SERVICE,
                new JMSMessageSender(trackerId, ACTIVE_MQ_SERVER, DATA_SYNC_SERVICE)
        );
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
        new Thread(() -> {
            trackerWindow = new TrackerWindow(getCurrentTrackerInstance());
            trackerWindow.setVisible(true);
            setRefresh(trackerWindow);
        }).start();
    }

    private TrackerInstance getCurrentTrackerInstance() {
        return this;
    }

    public void beginMasterElectionProcess() {
        System.out.println(trackerId + " Master election process begin");
        beginElections();
    }

    private void beginElections() {
        if (notEnoughNodesForVotation()) {
            System.out.println(trackerId + " setting as local MASTER");
            nodeType = TrackerInstanceNodeType.MASTER;
        } else {
            this.masterNode = getOlderTrackerInstance();
            System.out.println(trackerId + " Cluster MASTER node is: " + masterNode.getTrackerId());
            updateSelfNodeType();
        }
    }

    private TrackerInstance getOlderTrackerInstance() {
        Map<String, TrackerInstance> copiedTrackerList = (Map<String, TrackerInstance>) trackerNodeList.clone();
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
    }

    public void _debug_election_result() {
        for (Object o : getTrackerNodeList().entrySet()) {
            Map.Entry pair = (Map.Entry) o;
            TrackerInstance instance = (TrackerInstance) pair.getValue();
            System.out.println("\tDEBUG: " + this.trackerId + " knows that " + instance.trackerId + " is now " + instance.getNodeType());
        }
        System.out.println(trackerId + " Cluster MASTER node is: " + masterNode.getTrackerId());
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

    private synchronized void thread(Runnable runnable, boolean daemon) {
        Thread brokerThread = new Thread(runnable);
        brokerThread.setDaemon(daemon);
        brokerThread.start();
    }

    public synchronized void deploy() throws JMSException {
        //send first hello world message for tracker master detection
        MessageCollection message = MessageCollection.HELLO_WORLD;
        getSender(HANDSHAKE_SERVICE).send(message);
    }

    public JMSMessageSender getSender(TrackerDaemonSpec spec) {
        return senderHashMap.get(spec);
    }

    public JMSMessageListener getListener(TrackerDaemonSpec spec) {
        return listenerHashMap.get(spec);
    }

    public String getTrackerId() {
        return trackerId;
    }

    public TrackerInstanceNodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(TrackerInstanceNodeType nodeType) {
        this.nodeType = nodeType;
    }

    public HashMap<String, TrackerInstance> getTrackerNodeList() {
        return (HashMap<String, TrackerInstance>) trackerNodeList.clone();
    }

    public void addRemoteNode(TrackerInstance remoteNode) {
        this.trackerNodeList.put(remoteNode.getTrackerId(), remoteNode);
    }

    public void removeRemoteNode(String sourceTrackerId) {
        TrackerInstance node = TrackerInstance.getNode(sourceTrackerId);
        System.out.println(trackerId + " Removing remote node " + node + "from active trackers list");
        if (node != null) {
            removeNodeFromList(node);
            removeMasterNodeStatus(node);
        }
    }

    private void removeMasterNodeStatus(TrackerInstance node) {
        //eliminar el master en caso de que coincida
        if (node.equals(masterNode)) {
            masterNode = null;
        }
    }

    private boolean removeNodeFromList(TrackerInstance node) {
        TrackerInstance removed = this.trackerNodeList.remove(node.getTrackerId());
        boolean success = removed != null;
        if (success) {
            System.out.println(trackerId + " Successfully removed node " + node.getTrackerId());
        } else {
            System.out.println(trackerId + " Failed removing node " + node.getTrackerId());
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
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public TrackerStatus getTrackerStatus() {
        return trackerStatus;
    }

    public void setTrackerStatus(TrackerStatus trackerStatus) {
        this.trackerStatus = trackerStatus;
    }

    public boolean isMaster() {
        return nodeType == TrackerInstanceNodeType.MASTER;
    }

    public void stopNode() {
        sayGoodByeToCluster();
        stopSendingKeepAlives();
        // TODO stop all running background threads
    }

    private void sayGoodByeToCluster() {
        try {
            JMSMessageSender sender = this.getSender(HANDSHAKE_SERVICE);
            if (sender != null) {
                sender.send(MessageCollection.BYE_BYE);
            }
            this.setTrackerStatus(TrackerStatus.OFFLINE);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    private void stopSendingKeepAlives() {
        //stop sending keep alives
        this.nodeAlive.set(NODE_OFFLINE_MODE);
    }

    public void setRefresh(InterfaceRefresher refresh) {
        this.refresh = refresh;
    }

    public boolean isAlreadyDiscovered(TrackerInstance node) {
        return trackerNodeList.get(node.getTrackerId()) != null;
    }

    public void resetRemoteNodeTimeInLocalRegistry(TrackerInstance remoteNode) {
        System.out.println(this.getTrackerId() + " resetting " + remoteNode.getTrackerId() + " KEEP_ALIVE value");
        this.pendingLifetime.set(MAX_KEEP_ALIVE_TIME);
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

    public void updateNodeTable(HashMap<String, TrackerInstance> remoteNodeList) {
        if (refresh != null) {
            refresh.addTrackerNodeToTable(remoteNodeList);
        }
    }

    public int getLastKeepAlive() {
        return pendingLifetime.get();
    }

    public void decreasePendingLifeTime() {
        this.pendingLifetime.decrementAndGet();
    }

    public void removeDeadNodesFromList(ArrayList<TrackerInstance> instancesToRemove) {
        for (TrackerInstance node : instancesToRemove) {
            this.trackerNodeList.remove(node);
        }
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
        return this.nodeAlive.get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TrackerInstance that = (TrackerInstance) o;
        return trackerId.equals(that.trackerId);
    }

    public void requestDatabaseClone(TrackerInstance remoteNode) {
        //todo open a tcp connection against remote node for .sqlite file tranfer
        try{
            String remoteIp = remoteNode.getIp();
            int remotePort = remoteNode.getPort();
            Socket socket = new Socket(remoteIp, remotePort);

            //read stream
            DataInputStream in = new DataInputStream(socket.getInputStream());
            int size = Integer.parseInt(in.readLine().split(": ")[1]);
            byte[] item = new byte[size];
            for(int i = 0; i < size; i++)
                item[i] = in.readByte();

            //write to disk
            String fileName = getTrackerDatabaseName();
            File file = new File(fileName);
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            bos.write(item);

            bos.close();
            fos.close();

            in.close();
            socket.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    private String getTrackerDatabaseName() {
        return this.trackerId+".db";
    }
}