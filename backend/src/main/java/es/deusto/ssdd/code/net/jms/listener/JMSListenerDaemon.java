package es.deusto.ssdd.code.net.jms.listener;

import es.deusto.ssdd.code.net.jms.message.JMSMessageParser;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

/**
 * Created by .local on 08/11/2016.
 */
public class JMSListenerDaemon implements Runnable, ExceptionListener {

    private JMSMessageParser parser;
    private String connectionId;
    private String serviceName;
    private boolean serviceEnabled;
    private Connection connection;
    private Session session;
    private Destination destination;
    private MessageConsumer consumer;
    private String trackerId;

    public JMSListenerDaemon(String trackerId, String connectionId, String serviceName) {
        this.trackerId = trackerId;
        this.parser = new JMSMessageParser(trackerId, connectionId, serviceName);
        try {
            if (serviceName == null) {
                throw new JMSException("There is no service name defined before setting up JMS Listener");
            }
            if (connectionId == null) {
                throw new JMSException("There is JMS service ID  defined before setting up JMS Listener");
            }
            this.connectionId = connectionId;
            this.serviceName = serviceName;
            serviceEnabled = true;
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        System.out.println("JMS Daemon listener [STARTED]");
        while (serviceEnabled) {
            runDaemonListener();
        }
        System.out.println("JMS Daemon listener [STOPPED]");
    }

    private void runDaemonListener() {
        try {
            // Create a Connection
            connection = createConnection();

            // Create a Session
            session = createSession(connection);

            // Create the destination Queue
            destination = createDestinationTopic(session);

            // Create a MessageConsumer from the Session to the Queue
            consumer = createConsumer(session, destination);

            // Wait for a message
            readMessage(consumer);

            closeListener(connection, session, consumer);
        } catch (Exception ex) {
            onException(ex);
        }
    }

    private void closeListener(Connection connection, Session session, MessageConsumer consumer) {
        closeConsumer(consumer);
        closeSession(session);
        closeConnection(connection);
    }

    private void closeConsumer(MessageConsumer consumer) {
        if (consumer != null) {
            try {
                consumer.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeSession(Session s) {
        if (s != null) {
            try {
                s.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }

    private MessageConsumer createConsumer(Session session, Destination destination) throws JMSException {
        if (session != null && destination != null)
            return session.createConsumer(destination);
        throw new JMSException("No session or destination defined when attempting to create a consumer");
    }

    private void readMessage(MessageConsumer consumer) throws JMSException {
        if (consumer != null) {
            Message message = consumer.receive(1000);
            processReceivedMessage(message);
        }
    }

    private void processReceivedMessage(Message message) throws JMSException {
        if (message != null) {
            parser.process(message);
        }
    }

    private Destination createDestinationQueue(Session session) throws JMSException {
        return session.createQueue(serviceName);
    }

    private Destination createDestinationTopic(Session session) throws JMSException {
        return session.createTopic(serviceName);
    }

    private Session createSession(Connection connection) throws JMSException {
        if (connection != null)
            return connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        throw new JMSException("No connection defined when attempting to create a session");
    }

    private Connection createConnection() throws JMSException {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(connectionId);
        Connection connection = connectionFactory.createConnection();
        if (connection != null) {
            connection.start();
            connection.setExceptionListener(this);
            return connection;
        }
        throw new JMSException("No connection created when attempting to create a stable Connection");
    }

    public synchronized void onException(JMSException ex) {
        System.err.println("# JMS Listener Daemon Exception occured: " + ex.getMessage());
    }

    public synchronized void onException(Exception ex) {
        System.err.println("# JMS Listener Daemon Exception occured: " + ex.getMessage());
    }

    //SETTERS Y GETTERS

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public boolean isServiceEnabled() {
        return serviceEnabled;
    }

    public void setServiceEnabled(boolean serviceEnabled) {
        this.serviceEnabled = serviceEnabled;
    }
}
