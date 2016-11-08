package jms.helloworld;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;

public class HelloWorldConsumer implements Runnable, ExceptionListener {
	public void run() {
		try {
			// Create a ConnectionFactory
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");

			// Create a Connection
			Connection connection = connectionFactory.createConnection();
			connection.start();
			connection.setExceptionListener(this);

			// Create a Session
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			// Create the destination Queue
			Destination destination = session.createQueue("ssdd.helloWorld.queue");

			// Create a MessageConsumer from the Session to the Queue
			MessageConsumer consumer = session.createConsumer(destination);

			// Wait for a message
			Message message = consumer.receive(1000);

			if (message.getClass().equals(ActiveMQTextMessage.class)) {
				TextMessage textMessage = (TextMessage) message;
				System.err.println("<- Received TextMessage: " + textMessage.getText());
			} else {
				System.err.println("<- Received a Message: " + message);
			}

			consumer.close();
			session.close();
			connection.close();
		} catch (Exception ex) {
			System.err.println("# HelloWorldConsumer error: " + ex.getMessage());
		}
	}

	public synchronized void onException(JMSException ex) {
		System.err.println("# JMS Exception occured: " + ex.getMessage());
	}
}