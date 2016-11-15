package es.deusto.ssdd.code.net.jms;

import es.deusto.ssdd.code.net.bittorrent.core.TrackerUtil;

import javax.jms.JMSException;

/**
 * Created by .local on 14/11/2016.
 */
public class TrackerInstance {

    private static final String ACTIVE_MQ_SERVER = "tcp://localhost:61616";
    private static final String REMOTE_SERVICE = "tracker.handshake";
    private static int counter = 0;
    private final String trackerId;
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
        TrackerMessage message = TrackerMessage.HELLO_WORLD;
        sender.send(message);
    }

    public String getTrackerId() {
        return trackerId;
    }
}