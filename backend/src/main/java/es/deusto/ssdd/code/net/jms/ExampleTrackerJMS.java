package es.deusto.ssdd.code.net.jms;

/**
 * Created by .local on 08/11/2016.
 */
public class ExampleTrackerJMS {

    private static final String ACTIVE_MQ_SERVER = "tcp://localhost:61616";

    public static void main(String[] args) throws Exception {
        String serviceName = "ssdd.helloWorld.queue";
        thread(new JMSListenerDaemon(ACTIVE_MQ_SERVER, serviceName), false);
        /*
        thread(new JMSSenderDaemon(ACTIVE_MQ_SERVER, serviceName), false);
        thread(new JMSSenderDaemon(ACTIVE_MQ_SERVER, serviceName), false);
        thread(new JMSListenerDaemon(ACTIVE_MQ_SERVER, serviceName),, false);
        Thread.sleep(1000);
        thread(new JMSListenerDaemon(ACTIVE_MQ_SERVER, serviceName),, false);
        thread(new JMSSenderDaemon(ACTIVE_MQ_SERVER, serviceName), false);
        thread(new JMSListenerDaemon(ACTIVE_MQ_SERVER, serviceName),, false);
        thread(new JMSSenderDaemon(ACTIVE_MQ_SERVER, serviceName), false);
        Thread.sleep(1000);
        thread(new JMSListenerDaemon(ACTIVE_MQ_SERVER, serviceName),, false);
        thread(new JMSSenderDaemon(ACTIVE_MQ_SERVER, serviceName), false);
        thread(new JMSListenerDaemon(ACTIVE_MQ_SERVER, serviceName),, false);
        thread(new JMSListenerDaemon(ACTIVE_MQ_SERVER, serviceName),, false);
        thread(new JMSSenderDaemon(ACTIVE_MQ_SERVER, serviceName), false);
        thread(new JMSSenderDaemon(ACTIVE_MQ_SERVER, serviceName), false);
        Thread.sleep(1000);
        thread(new JMSSenderDaemon(ACTIVE_MQ_SERVER, serviceName), false);
        thread(new JMSListenerDaemon(ACTIVE_MQ_SERVER, serviceName),, false);
        thread(new JMSListenerDaemon(ACTIVE_MQ_SERVER, serviceName),, false);
        thread(new JMSSenderDaemon(ACTIVE_MQ_SERVER, serviceName), false);
        thread(new JMSListenerDaemon(ACTIVE_MQ_SERVER, serviceName),, false);
        thread(new JMSSenderDaemon(ACTIVE_MQ_SERVER, serviceName), false);
        thread(new JMSListenerDaemon(ACTIVE_MQ_SERVER, serviceName),, false);
        thread(new JMSSenderDaemon(ACTIVE_MQ_SERVER, serviceName), false);
        thread(new JMSListenerDaemon(ACTIVE_MQ_SERVER, serviceName),, false);
        thread(new JMSListenerDaemon(ACTIVE_MQ_SERVER, serviceName),, false);
        thread(new JMSSenderDaemon(ACTIVE_MQ_SERVER, serviceName), false);
         */
    }

    public static void thread(Runnable runnable, boolean daemon) {
        Thread brokerThread = new Thread(runnable);
        brokerThread.setDaemon(daemon);
        brokerThread.start();
    }
}
