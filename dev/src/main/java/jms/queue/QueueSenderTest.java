package jms.queue;

import java.util.Calendar;

import javax.jms.MapMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

public class QueueSenderTest {
	
	public static void main(String args[]) {		
		String connectionFactoryName = "QueueConnectionFactory";
		String queueJNDIName = "jndi.ssdd.queue";		
		
		QueueConnection queueConnection = null;
		QueueSession queueSession = null;
		QueueSender queueSender = null;			
		
		try{
			//JNDI Initial Context
			Context ctx = new InitialContext();
		
			//Connection Factory
			QueueConnectionFactory queueConnectionFactory = (QueueConnectionFactory) ctx.lookup(connectionFactoryName);
			
			//Message Destination
			Queue myQueue = (Queue) ctx.lookup(queueJNDIName);			
	
			//Connection	
			queueConnection = queueConnectionFactory.createQueueConnection();
			System.out.println("- Queue Connection created!");
			
			//Session
			queueSession = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			System.out.println("- Queue Session created!");
	
			//Message Producer
			queueSender = queueSession.createSender(myQueue);			
			System.out.println("- QueueSender created!");
			
			//Text Message
			TextMessage textMessage = queueSession.createTextMessage();
			//Message Properties
			textMessage.setStringProperty("Filter", "1");
			//Message Body
			textMessage.setText("Hello World!!");			
			
			//Map Message			
			MapMessage mapMessage = queueSession.createMapMessage();
			//Message Properties
			mapMessage.setStringProperty("Filter", "2");				
			//Message Body
			mapMessage.setString("Text", "Hello World!");
			mapMessage.setLong("Timestamp", Calendar.getInstance().getTimeInMillis());
			mapMessage.setBoolean("ACK_required", true);
						
			//Send the Messages
			queueSender.send(textMessage);
			System.out.println("- TextMessage sent to the Queue!");
			queueSender.send(mapMessage);
			System.out.println("- MapMessage sent to the Queue!");
		} catch (Exception e) {
			System.err.println("# QueueSenderTest Error: " + e.getMessage());
		} finally {
			try {
				queueSender.close();
				queueSession.close();
				queueConnection.close();
				System.out.println("- Queue resources closed!");				
			} catch (Exception ex) {
				System.err.println("# QueueSenderTest Error: " + ex.getMessage());
			}
		}
	}
}