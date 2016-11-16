package es.deusto.ssdd.code.net.jms;

import es.deusto.ssdd.code.net.bittorrent.core.TrackerUtil;
import es.deusto.ssdd.code.net.gui.view.TrackerWindow;
import es.deusto.ssdd.code.net.jms.listener.JMSMessageListener;
import es.deusto.ssdd.code.net.jms.listener.JMSMessageSender;
import es.deusto.ssdd.code.net.jms.listener.TrackerDaemonSpec;
import es.deusto.ssdd.code.net.jms.message.MessageCollection;
import es.deusto.ssdd.code.net.jms.model.TrackerInstanceNodeType;
import es.deusto.ssdd.code.net.jms.model.TrackerStatus;

import javax.jms.JMSException;
import java.util.ArrayList;
import java.util.HashMap;

import static es.deusto.ssdd.code.net.jms.listener.TrackerDaemonSpec.*;

/**
 * Created by .local on 14/11/2016.
 */
public class TrackerInstance implements Comparable{

    private static final String ACTIVE_MQ_SERVER = "tcp://localhost:61616";

    private static final int THIS_IS_OLDER = 1;
    private static final int TIMESTAMP_VALUE = 6;
    private static int counter = 0;

    private static final HashMap<String, TrackerInstance> map = new HashMap<>();

    //listener map
    private HashMap<TrackerDaemonSpec, JMSMessageListener> listenerHashMap;

    //sender map
    private HashMap<TrackerDaemonSpec, JMSMessageSender> senderHashMap;

    private final String trackerId;

    private TrackerInstanceNodeType nodeType;

    //master node
    private TrackerInstance masterNode;

    //tracker node list
    private final ArrayList<TrackerInstance> trackerNodeList;

    private TrackerWindow trackerWindow;
    private String ip;
    private int port;
    private TrackerStatus trackerStatus;

    public TrackerInstance() {

        this. trackerStatus = TrackerStatus.OFFLINE;

        System.out.println("Running tracker instance " + (counter + 1));
        counter++;

        trackerId = TrackerUtil.getDeviceMacAddress() + ":" + System.nanoTime();
        ip = TrackerUtil.getIP();
        port = 8000;
        System.out.println("Tracker ID: " + trackerId);

        //add to instance map. only for development with multinodes in local mode
        TrackerInstance.map.put(trackerId, this);

        //init tracker node list
        trackerNodeList = new ArrayList<>();

        setupDaemons();

        //add itself to tracker node list
        trackerNodeList.add(this);

        //init master node
        masterNode = null;

        beginMasterElectionProcess();

        //deploy our background services
        //thread(listener, false);
        //thread(sender, false);

        this. trackerStatus = TrackerStatus.ONLINE;

        showTrackerWindow();
    }

    private void setupDaemons() {
        //init maps
        senderHashMap = new HashMap<>();
        listenerHashMap = new HashMap<>();

        setupSenderDaemons();
        setupListenerDaemons();
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
            //show this tracker related window
            trackerWindow = new TrackerWindow(getCurrentTrackerInstance());
            trackerWindow.setVisible(true);
        }).start();
    }

    private TrackerInstance getCurrentTrackerInstance() {
        return this;
    }

    private void beginMasterElectionProcess() {
        if(masterNode==null){
            System.out.println(trackerId + " Master election process begin");
            if(trackerNodeList.size()==1){
                System.out.println(trackerId + " setting as MASTER");
                nodeType = TrackerInstanceNodeType.MASTER;
            }
            else{
                //calculate older node from list and at the same time, update their roll: master or slave
                TrackerInstance olderIdInstance = trackerNodeList.get(0);
                for(TrackerInstance instance : trackerNodeList){
                    olderIdInstance = this.getOlderTrackerNode(olderIdInstance, instance);
                }
                this.masterNode = olderIdInstance;

                _debug_election_result();
            }
        }
        else{
            System.out.println(trackerId + " Master node ("+masterNode.getTrackerId()+") already known. Election process [ABORT]");
        }
    }

    private void _debug_election_result() {
        for(TrackerInstance instance : trackerNodeList){
            System.out.println("\tDEBUG: "+this.trackerId+" knows that "+instance.trackerId+" is now "+instance.getNodeType());
        }

        System.out.println(trackerId+" Cluster MASTER node is: "+masterNode.getTrackerId());
    }

    private TrackerInstance getOlderTrackerNode(TrackerInstance olderIdInstance, TrackerInstance instance) {
        int result = olderIdInstance.compareTo(instance);
        if(result==0){
            return olderIdInstance;
        }
        else if(result>=1){
            instance.setNodeType(TrackerInstanceNodeType.MASTER);
            olderIdInstance.setNodeType(TrackerInstanceNodeType.SLAVE);
            return instance;
        }
        else{
            instance.setNodeType(TrackerInstanceNodeType.SLAVE);
            olderIdInstance.setNodeType(TrackerInstanceNodeType.MASTER);
            return olderIdInstance;
        }
    }

    private void thread(Runnable runnable, boolean daemon) {
        Thread brokerThread = new Thread(runnable);
        brokerThread.setDaemon(daemon);
        brokerThread.start();
    }

    public void deploy() throws JMSException {
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

    public static TrackerInstance getNode(String id) {
        return TrackerInstance.map.get(id);
    }

    public void addRemoteNode(String sourceTrackerId) {
        System.out.println("Adding remote node "+sourceTrackerId+" to active trackers list");
        TrackerInstance node = TrackerInstance.getNode(sourceTrackerId);
        if(node!=null){
            //añadir a la lista de nodos
            this.trackerNodeList.add(node);
            //como se ha añadido un nodo, evaluar otra vez la eleccion del master
            this.beginMasterElectionProcess();
        }
    }

    @Override
    public int compareTo(Object o) {
        if(o.getClass().equals(TrackerInstance.class)){
            TrackerInstance i = (TrackerInstance) o;
            return this.getTrackerTimeStamp().compareTo(i.getTrackerTimeStamp());
        }
        return THIS_IS_OLDER;
    }

    private String getTrackerTimeStamp() {
        String[] data = getTrackerId().split(":");
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
}