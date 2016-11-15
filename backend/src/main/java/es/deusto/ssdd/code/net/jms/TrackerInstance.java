package es.deusto.ssdd.code.net.jms;

import es.deusto.ssdd.code.net.bittorrent.core.TrackerUtil;
import es.deusto.ssdd.code.net.jms.listener.JMSMessageListener;
import es.deusto.ssdd.code.net.jms.listener.JMSMessageSender;
import es.deusto.ssdd.code.net.jms.message.MessageCollection;
import es.deusto.ssdd.code.net.jms.model.TrackerInstanceNodeType;

import javax.jms.JMSException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by .local on 14/11/2016.
 */
public class TrackerInstance implements Comparable{

    private static final String ACTIVE_MQ_SERVER = "tcp://localhost:61616";
    private static final String REMOTE_SERVICE = "tracker.handshake";
    private static final int THIS_IS_OLDER = 1;
    private static final int TIMESTAMP_VALUE = 6;
    private static int counter = 0;

    private final String trackerId;
    private TrackerInstanceNodeType nodeType;

    //master node
    private TrackerInstance masterNode;

    //tracker node list
    private final ArrayList<TrackerInstance> trackerNodeList;

    private JMSMessageListener listener;
    private JMSMessageSender sender;

    private static final HashMap<String, TrackerInstance> map = new HashMap<>();

    public TrackerInstance() {
        System.out.println("Running tracker instance " + (counter + 1));
        counter++;

        trackerId = TrackerUtil.getDeviceMacAddress() + ":" + System.nanoTime();
        System.out.println("Tracker ID: " + trackerId);

        //add to instance map. only for development with multinodes in local mode
        TrackerInstance.map.put(trackerId, this);

        //define our background services
        listener = new JMSMessageListener(trackerId, ACTIVE_MQ_SERVER, REMOTE_SERVICE);
        sender = new JMSMessageSender(trackerId, ACTIVE_MQ_SERVER, REMOTE_SERVICE);

        //init tracker node list
        trackerNodeList = new ArrayList<>();

        //add itself to tracker node list
        trackerNodeList.add(this);

        //init master node
        masterNode = null;

        beginMasterElectionProcess();

        //sayHelloToTrackerNodesCluster our background services
        thread(listener, false);
        thread(sender, false);
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

    public void sayHelloToTrackerNodesCluster() throws JMSException {
        //send first hello world message for tracker master detection
        MessageCollection message = MessageCollection.HELLO_WORLD;
        sender.send(message);
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
}