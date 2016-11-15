package es.deusto.ssdd.code.net.jms;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.ArrayList;

public class JMSSenderDaemon implements Runnable {

    private final String trackerId;
    private String serviceName;
    private String connectionId;
    private MessageProducer producer;
    private Connection connection;
    private Destination destination;
    private Session session;
    private boolean keepAlive;
    private ArrayList<TrackerMessage> messagesToSend;

    public JMSSenderDaemon(String trackerId, String connectionId, String serviceName) {
        this.trackerId = trackerId;
        try {
            if (serviceName == null)
                throw new JMSException("A service name is needed for JMS Message sender creation");
            if (connectionId == null)
                throw new JMSException("A server connection ID is needed for JMS Message sender creation");
            this.serviceName = serviceName;
            this.connectionId = connectionId;
            this.keepAlive = true;
            this.messagesToSend = new ArrayList<>();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        System.out.println("JMS Daemon sender [STARTED]");
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
                    Message message = messagesToSend.remove(0).getMessage(this);
                    this.sendMessage(producer, message);
                }
            }

            // Clean up
            closeSender(connection, session);
            System.out.println("JMS Daemon sender [STOPPED]");
        } catch (Exception ex) {
            System.err.println("# JMSSenderDaemon error: " + ex.getMessage());
        }
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

    private TextMessage createMessage(Session session, String text) throws JMSException {
        if (session == null)
            throw new JMSException("There is no valid session to use for message creation");
        if (text == null) {
            throw new JMSException("There is no valid text to create a message");
        }
        return session.createTextMessage(text);
    }

    private void sendMessage(MessageProducer producer, Message message) throws JMSException {
        // Tell the producer to send the message
        if (producer == null)
            throw new JMSException("There is no message producer defined for sending a message");
        if (message == null) {
            throw new JMSException("There is no valid message to send");
        }
        System.out.println(trackerId + " >> SEND >> "+serviceName+"/"+connectionId +" >> "+ message.toString());
        producer.send(message);
    }

    private void closeSender(Connection connection, Session session) throws JMSException {
        if (connection == null)
            throw new JMSException("There is no connection to close");
        if (session == null)
            throw new JMSException("There is no session to close");
        session.close();
        connection.close();
    }

    public boolean send(TrackerMessage message) throws JMSException {
        System.out.println("Waiting message " + message + " to be sent");
        if (message != null) {
            this.messagesToSend.add(message);
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

    public ArrayList<TrackerMessage> getMessagesToSend() {
        return messagesToSend;
    }

    public void setMessagesToSend(ArrayList<TrackerMessage> messagesToSend) {
        this.messagesToSend = messagesToSend;
    }

    public String getTrackerId() {
        return trackerId;
    }
}