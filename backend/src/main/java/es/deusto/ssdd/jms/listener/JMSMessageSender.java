package es.deusto.ssdd.jms.listener;

import es.deusto.ssdd.jms.TrackerInstance;
import es.deusto.ssdd.jms.message.IJMSMessage;
import es.deusto.ssdd.jms.message.MessageCollection;
import es.deusto.ssdd.jms.model.TrackerDaemonSpec;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQObjectMessage;

import javax.jms.*;
import java.util.ArrayList;

public class JMSMessageSender implements Runnable {

    private final TrackerInstance tracker;
    private final String trackerId;
    private String serviceName;
    private String connectionId;

    private MessageProducer producer;
    private Connection connection;
    private Destination destination;
    private Session session;
    private boolean keepAlive;
    private ArrayList<MessageCollection> messagesToSend;

    public JMSMessageSender(TrackerInstance tracker, String connectionId, TrackerDaemonSpec trackerSpecs) {
        this.tracker = tracker;
        this.trackerId = tracker.getTrackerId();
        try {
            if (trackerSpecs == null)
                throw new JMSException("A tracker service spec is needed for JMS Message sender creation");
            if (connectionId == null)
                throw new JMSException("A server connection ID is needed for JMS Message sender creation");
            this.serviceName = trackerSpecs.getServiceName();
            this.connectionId = connectionId;
            this.keepAlive = true;
            this.messagesToSend = new ArrayList<>();
        } catch (JMSException e) {
            TrackerInstance.getNode(trackerId).addLogLine("Error: " + e.getLocalizedMessage());
        }
    }

    public void run() {
        TrackerInstance.getNode(trackerId).addLogLine("Debug: JMS " + getMessageSenderId() + " Daemon sender [STARTED]");
        try {
            // Create a Connection
            connection = createConnection(connectionId);

            // Create a Session
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // Create the destination Queue
            destination = createTopic(session);

            // Create a MessageProducer from the Session to the Queue
            producer = createMessageProducer(session, destination);

            while (keepAlive) {
                if (!messagesToSend.isEmpty()) {
                    //get the first message on the queue and send it
                    MessageCollection jmsMessage = messagesToSend.remove(0);
                    ActiveMQObjectMessage message = (ActiveMQObjectMessage) jmsMessage.getMessage(this);
                    this.sendMessage(producer, message);
                    this.triggerMessageSendAction(message);
                }
                Thread.sleep(50);
            }

            // Clean up
            closeSender(connection, session);
            TrackerInstance.getNode(trackerId).addLogLine("Debug: JMS Daemon sender [STOPPED]");
        } catch (Exception ex) {
            TrackerInstance.getNode(trackerId).addLogLine("Error: JMSMessageSender error: " + ex.getMessage());
        }
    }

    private String getMessageSenderId() {
        return "[ " + connectionId + "/" + serviceName + " ]";
    }

    private void triggerMessageSendAction(ActiveMQObjectMessage message) {
        new Thread(() -> {
            if (message != null) {
                try {
                    Object o = message.getObject();
                    if (o != null) {
                        IJMSMessage m = (IJMSMessage) o;
                        TrackerInstance.getNode(trackerId).addLogLine("Stream: >> SEND >> " + getMessageSenderId() + " >> " + m.getPrintable());
                        m.onBroadcastEvent(trackerId);
                    }
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private Connection createConnection(String connectionId) throws JMSException {
        if (connectionId == null)
            throw new JMSException("There is no valid connection ID for creating a connection to JMS");
        // Create a ConnectionFactory
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(connectionId);
        Connection connection = connectionFactory.createConnection();
        connection.start();
        return connection;
    }

    private Queue createQueue(Session session) throws JMSException {
        if (session == null)
            throw new JMSException("There is no valid session to use for queue creation");
        return session.createQueue(serviceName);
    }

    private Topic createTopic(Session session) throws JMSException {
        if (session == null)
            throw new JMSException("There is no valid session to use for topic creation");
        return session.createTopic(serviceName);
    }

    private MessageProducer createMessageProducer(Session session, Destination destination) throws JMSException {
        if (session == null)
            throw new JMSException("There is no valid session to use for producer creation");
        if (destination == null) {
            throw new JMSException("There is no valid destination for producer creation");
        }
        MessageProducer producer = session.createProducer(destination);
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        return producer;
    }

    private void sendMessage(MessageProducer producer, Message message) throws JMSException {
        // Tell the producer to send the message
        if (producer == null)
            throw new JMSException("There is no message producer defined for sending a message");
        if (message == null) {
            throw new JMSException("There is no valid message to send");
        }
        new Thread(() -> {
            try {
                producer.send(message);
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void closeSender(Connection connection, Session session) throws JMSException {
        if (connection == null)
            throw new JMSException("There is no connection to close");
        if (session == null)
            throw new JMSException("There is no session to close");
        session.close();
        connection.close();
    }

    public boolean send(MessageCollection message) throws JMSException {
        if (message != null) {
            this.messagesToSend.add(message);
            return true;
        }
        return false;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public MessageProducer getProducer() {
        return producer;
    }

    public void setProducer(MessageProducer producer) {
        this.producer = producer;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public ArrayList<MessageCollection> getMessagesToSend() {
        return messagesToSend;
    }

    public void setMessagesToSend(ArrayList<MessageCollection> messagesToSend) {
        this.messagesToSend = messagesToSend;
    }

    public String getTrackerId() {
        return trackerId;
    }

    public TrackerInstance getTracker() {
        return tracker;
    }
}