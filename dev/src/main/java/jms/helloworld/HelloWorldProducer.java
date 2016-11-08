package jms.helloworld;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

public class HelloWorldProducer implements Runnable {
	public void run() {			
		try {
			// Create a ConnectionFactory
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");

			// Create a Connection
			Connection connection = connectionFactory.createConnection();
			connection.start();

			// Create a Session
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			// Create the destination Queue
			Destination destination = session.createQueue("ssdd.helloWorld.queue");

			// Create a MessageProducer from the Session to the Queue
			MessageProducer producer = session.createProducer(destination);
			producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

			// Create a messages
			String text = "Hello world! From: " + Thread.currentThread().getName() + " : " + this.hashCode();
			TextMessage message = session.createTextMessage(text);

			// Tell the producer to send the message
			System.out.println("-> Sent message: " + message.hashCode() + " : " + Thread.currentThread().getName());
			producer.send(message);

			// Clean up
			session.close();
			connection.close();
		} catch (Exception ex) {
			System.err.println("# HelloWorldProducer error: " + ex.getMessage());
		}
	}
}