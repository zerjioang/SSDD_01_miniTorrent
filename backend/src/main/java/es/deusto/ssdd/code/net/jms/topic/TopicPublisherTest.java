package es.deusto.ssdd.code.net.jms.topic;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Calendar;

public class TopicPublisherTest {

    public static void main(String[] args) {
        String connectionFactoryName = "TopicConnectionFactory";
        String topicJNDIName = "jndi.ssdd.topic";

        TopicConnection topicConnection = null;
        TopicSession topicSession = null;
        TopicPublisher topicPublisher = null;

        try {
            //JNDI Initial Context
            Context ctx = new InitialContext();

            //Connection Factory
            TopicConnectionFactory topicConnectionFactory = (TopicConnectionFactory) ctx.lookup(connectionFactoryName);

            //Message Destination
            Topic myTopic = (Topic) ctx.lookup(topicJNDIName);

            //Connection
            topicConnection = topicConnectionFactory.createTopicConnection();
            System.out.println("- Topic Connection created!");

            //Session
            topicSession = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            System.out.println("- Topic Session created!");

            //Message Publisher
            topicPublisher = topicSession.createPublisher(myTopic);
            System.out.println("- TopicPublisher created!");

            //Text Message
            TextMessage textMessage = topicSession.createTextMessage();
            //Message Headers
            textMessage.setJMSType("TextMessage");
            textMessage.setJMSMessageID("ID-1");
            textMessage.setJMSPriority(1);
            //Message Properties
            textMessage.setStringProperty("Filter", "1");
            //Message Body
            textMessage.setText("Hello World!!");

            //Map Message
            MapMessage mapMessage = topicSession.createMapMessage();
            //Message Headers
            mapMessage.setJMSType("MapMessage");
            mapMessage.setJMSMessageID("ID-1");
            mapMessage.setJMSPriority(2);
            //Message Properties
            mapMessage.setStringProperty("Filter", "2");
            //Message Body
            mapMessage.setString("Text", "Hello World!");
            mapMessage.setLong("Timestamp", Calendar.getInstance().getTimeInMillis());
            mapMessage.setBoolean("ACK_required", true);

            //Publish the Messages
            topicPublisher.publish(textMessage);
            System.out.println("- TextMessage published in the Topic!");
            topicPublisher.publish(mapMessage);
            System.out.println("- MapMessage sent to the Topic!");
        } catch (Exception e) {
            System.err.println("# TopicPublisherTest Error: " + e.getMessage());
        } finally {
            try {
                //Close resources
                topicPublisher.close();
                topicSession.close();
                topicConnection.close();
                System.out.println("- Topic resources closed!");
            } catch (Exception ex) {
                System.err.println("# TopicPublisherTest Error: " + ex.getMessage());
            }
        }
    }
}
