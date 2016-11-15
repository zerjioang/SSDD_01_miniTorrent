package es.deusto.ssdd.code.net.jms;

import es.deusto.ssdd.code.net.bittorrent.core.TrackerUtil;
import es.deusto.ssdd.code.net.jms.listener.JMSListenerDaemon;
import es.deusto.ssdd.code.net.jms.listener.JMSSenderDaemon;
import es.deusto.ssdd.code.net.jms.message.MessageCollection;
import es.deusto.ssdd.code.net.jms.model.TrackerInstanceNodeType;

import javax.jms.JMSException;

/**
 * Created by .local on 14/11/2016.
 */
public class TrackerInstance {

    private static final String ACTIVE_MQ_SERVER = "tcp://localhost:61616";
    private static final String REMOTE_SERVICE = "tracker.handshake";
    private static int counter = 0;

    private final String trackerId;
    private TrackerInstanceNodeType nodeType;

    private JMSListenerDaemon listener;
    private JMSSenderDaemon sender;

    public TrackerInstance() {
        System.out.println("Running tracker instance " + (counter + 1));
        counter++;

        trackerId = TrackerUtil.getDeviceMacAddress() + ":" + System.nanoTime();

        System.out.println("Tracker ID: " + trackerId);

        //define our background services
        listener = new JMSListenerDaemon(trackerId, ACTIVE_MQ_SERVER, REMOTE_SERVICE);
        sender = new JMSSenderDaemon(trackerId, ACTIVE_MQ_SERVER, REMOTE_SERVICE);

        //init nodetype variable
        nodeType = null;

        //start our background services
        thread(listener, false);
        thread(sender, false);
    }

    private void thread(Runnable runnable, boolean daemon) {
        Thread brokerThread = new Thread(runnable);
        brokerThread.setDaemon(daemon);
        brokerThread.start();
    }

    public void start() throws JMSException {
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
}