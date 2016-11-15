package es.deusto.ssdd.code.net.jms.message;

import org.apache.activemq.command.ActiveMQObjectMessage;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * Created by .local on 15/11/2016.
 */
public class JMSMessageParser {
    private final String trackerId;
    private final String connectionId;
    private final String serviceName;

    public JMSMessageParser(String trackerId, String connectionId, String serviceName) {

        this.trackerId = trackerId;
        this.connectionId = connectionId;
        this.serviceName = serviceName;
    }

    public void process(String destinationNodeId, Message message) {
        try {
            if (message.getClass().equals(ActiveMQObjectMessage.class)) {
                ActiveMQObjectMessage objectMessage = (ActiveMQObjectMessage) message;
                Object o = objectMessage.getObject();
                if(o!=null){
                    IJMSMessage receivedMessage = (IJMSMessage) o;
                    if(isReceivedMessageMine(receivedMessage)){
                        //drop message
                        System.out.println(trackerId + " << DROP << "+serviceName+"/"+connectionId +" << "+ receivedMessage.getPrintable());
                    }
                    else{
                        //log communication
                        System.out.println(trackerId + " << RECEIVED << "+serviceName+"/"+connectionId +" << "+ receivedMessage.getPrintable());
                        //trigger action
                        receivedMessage.onReceivedEvent(destinationNodeId);
                    }
                }
            } else {
                System.out.println("<- Received a Message: " + message);
            }
        } catch (JMSException ex) {
            System.err.println("# JMS Listener MESSAGE PARSING Exception occured: " + ex.getMessage());
        }
    }

    private boolean isReceivedMessageMine(IJMSMessage receivedMessage) {
        return this.trackerId.equals(receivedMessage.getSourceTrackerId());
    }
}
