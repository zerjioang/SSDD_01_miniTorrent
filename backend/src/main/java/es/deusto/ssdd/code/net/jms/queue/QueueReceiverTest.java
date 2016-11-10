package es.deusto.ssdd.code.net.jms.queue;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;

public class QueueReceiverTest {

    public static void main(String[] args) {
        String connectionFactoryName = "QueueConnectionFactory";
        String queueJNDIName = "jndi.ssdd.queue";

        QueueConnection queueConnection = null;
        QueueSession queueSession = null;
        QueueReceiver queueReceiver = null;

        try {
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

            //Message Receiver
            queueReceiver = queueSession.createReceiver(myQueue, "Filter = '2'");
            System.out.println("- QueueReceiver created!");

            QueueMessageListener listener = new QueueMessageListener();

            queueReceiver.setMessageListener(listener);

            //Start receiving messages
            queueConnection.start();

			/*
			//Receive a message (1sec. timeout)
			Message message = queueReceiver.receive(1000);			
			
			if (message != null) {
				System.out.println("- Message received. Type: " + message.getClass().getSimpleName());
			} else {
				System.out.println("- Error receiving a message from the queue.");
			}
			
			//Receive another message (1sec. timeout)
			message = queueReceiver.receive(1000);
			
			if (message != null) {
				System.out.println("- Message received. Type: " + message.getClass().getSimpleName());
			} else {
				System.out.println("- Error receiving a message from the queue.");
			}*/

            Thread.sleep(10000);
        } catch (Exception e) {
            System.err.println("# QueueReceiverTest Error: " + e.getMessage());
        } finally {
            try {
                queueReceiver.close();
                queueSession.close();
                queueConnection.close();
                System.out.println("- Queue resources closed!");
            } catch (Exception ex) {
                System.err.println("# QueueReceiverTest Error: " + ex.getMessage());
            }
        }
    }
}
