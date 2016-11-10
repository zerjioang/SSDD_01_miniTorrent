package es.deusto.ssdd.code.net.jms;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.*;

/**
 * Created by .local on 08/11/2016.
 */
public class JMSListenerDaemon implements Runnable, ExceptionListener, TrackerMessageParser {

    private final String connectionId;
    private final String serviceName;
    private boolean serviceEnabled;

    public JMSListenerDaemon(String connectionId, String serviceName) throws JMSException {
        if (serviceName == null) {
            throw new JMSException("There is no service name defined before setting up JMS Listener");
        }
        if (connectionId == null) {
            throw new JMSException("There is JMS service ID  defined before setting up JMS Listener");
        }
        this.connectionId = connectionId;
        this.serviceName = serviceName;
        serviceEnabled = true;
    }

    public void run() {
        while (serviceEnabled) {
            System.out.println("JMS Daemon listener [STARTED]");
            runDaemonListener();
        }
        System.out.println("JMS Daemon listener [STOPPED]");
    }

    private void runDaemonListener() {
        try {
            // Create a Connection
            Connection connection = createConnection();

            // Create a Session
            Session session = createSession(connection);

            // Create the destination Queue
            Destination destination = createDestinationQueue(session);

            // Create a MessageConsumer from the Session to the Queue
            MessageConsumer consumer = createConsumer(session, destination);

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
        throw new JMSException("No consumer defined when attempting to read a message");
    }

    private void processReceivedMessage(Message message) throws JMSException {
        if (message != null) {
            parseMessageContent(message);
        }
        throw new JMSException("No message defined when attempting to parse it.");
    }

    private Destination createDestinationQueue(Session session) throws JMSException {
        return session.createQueue(serviceName);
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

    //INTERFACES OVERWRITE

    public void parseMessageContent(Message message) {
        try {
            if (message.getClass().equals(ActiveMQTextMessage.class)) {
                TextMessage textMessage = (TextMessage) message;
                System.err.println("<- Received TextMessage: " + textMessage.getText());
            } else {
                System.err.println("<- Received a Message: " + message);
            }
        } catch (JMSException ex) {
            System.err.println("# JMS Listener MESSAGE PARSING Exception occured: " + ex.getMessage());
        }
    }

    //SETTERS Y GETTERS

    public void setServiceEnabled(boolean serviceEnabled) {
        this.serviceEnabled = serviceEnabled;
    }
}
