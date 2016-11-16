package es.deusto.ssdd.code.net.jms.listener;

import es.deusto.ssdd.code.net.jms.message.IJMSMessage;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQObjectMessage;

import javax.jms.*;

/**
 * Created by .local on 08/11/2016.
 */
public class JMSMessageListener implements Runnable, ExceptionListener, MessageListener {

    private String connectionId;
    private String serviceName;
    private Connection connection;
    private Session session;
    private Destination destination;
    private MessageConsumer consumer;
    private String trackerId;

    public JMSMessageListener(String trackerId, String connectionId, TrackerDaemonSpec trackerDaemonSpec) {
        this.trackerId = trackerId;
        this.serviceName = trackerDaemonSpec.getServiceName();
        try {
            if (connectionId == null) {
                throw new JMSException("There is JMS service ID  defined before setting up JMS Listener");
            }
            this.connectionId = connectionId;
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        System.out.println(trackerId+" JMS Daemon listener [STARTED]");
        runDaemonListener();
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

            //listen for async messages
            consumer.setMessageListener(this);

            //start connection
            connection.setExceptionListener(this);
            connection.start();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void closeListener(Connection connection, Session session, MessageConsumer consumer) {
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

    private Destination createDestinationQueue(Session session) throws JMSException {
        if(session==null)
            throw new JMSException("No session defined when attempting to create a queue");
        return session.createQueue(serviceName);
    }

    private Destination createDestinationTopic(Session session) throws JMSException {
        if(session==null)
            throw new JMSException("No session defined when attempting to create a topic");
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
        if(connection==null)
            throw new JMSException("No connection created when attempting to create a stable Connection");
        return connection;
    }

    public synchronized void onException(JMSException ex) {
        System.err.println("# JMS Listener Daemon Exception occured: " + ex.getMessage());
    }

    @Override
    public void onMessage(Message message) {
        if(message!=null){
            try {
                if (message.getClass().equals(ActiveMQObjectMessage.class)) {
                    ActiveMQObjectMessage objectMessage = (ActiveMQObjectMessage) message;
                    Object o = objectMessage.getObject();
                    if(o!=null){
                        IJMSMessage receivedMessage = (IJMSMessage) o;
                        if(isReceivedMessageMine(receivedMessage)){
                            //drop message
                            System.out.println(trackerId + " << DROP << "+connectionId + "/"+ serviceName +" << "+ receivedMessage.getPrintable());
                        }
                        else{
                            //log communication
                            System.out.println(trackerId + " << RECEIVED << "+connectionId + "/" + serviceName +" << "+ receivedMessage.getPrintable());
                            //trigger action
                            receivedMessage.onReceivedEvent(this.trackerId);
                        }
                    }
                } else {
                    System.out.println("<- Received a Message: " + message);
                }
            } catch (JMSException ex) {
                System.err.println("# JMS Listener MESSAGE PARSING Exception occured: " + ex.getMessage());
            }
        }
    }

    private boolean isReceivedMessageMine(IJMSMessage receivedMessage) {
        return this.trackerId.equals(receivedMessage.getSourceTrackerId());
    }
}
