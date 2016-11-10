package es.deusto.ssdd.code.net.jms;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class JMSSenderDaemon implements Runnable {

    private String serviceName;
    private String connectionId;

    public JMSSenderDaemon(String connectionId, String serviceName) throws JMSException {
        if (serviceName == null)
            throw new JMSException("A service name is needed for JMS Message sender creation");
        if (connectionId == null)
            throw new JMSException("A server connection ID is needed for JMS Message sender creation");
        this.serviceName = serviceName;
        this.connectionId = connectionId;
    }

    public void run() {
        try {
            // Create a Connection
            Connection connection = createConnection(connectionId);

            // Create a Session
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // Create the destination Queue
            Destination destination = createQueue(session);

            // Create a MessageProducer from the Session to the Queue
            MessageProducer producer = createMessageProducer(session, destination);

            // Create a messages
            String text = "Hello world! From: " + Thread.currentThread().getName() + " : " + this.hashCode();
            TextMessage message = createMessage(session, text);

            sendMessage(producer, message);

            // Clean up
            closeSender(connection, session);
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

    private void sendMessage(MessageProducer producer, TextMessage message) throws JMSException {
        // Tell the producer to send the message
        if (producer == null)
            throw new JMSException("There is no message producer defined for sending a message");
        if (message == null) {
            throw new JMSException("There is no valid message to send");
        }
        System.out.println("-> Sent message: " + message.hashCode() + " : " + Thread.currentThread().getName());
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
}