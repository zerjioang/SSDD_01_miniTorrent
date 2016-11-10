package es.deusto.ssdd.code.net.jms.topic;

import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.Enumeration;

public class TopicListener implements MessageListener {

    @Override
    public void onMessage(Message message) {
        if (message != null) {
            try {
                System.out.println("   - TopicListener: " + message.getClass().getSimpleName() + " received!");

                if (message.getClass().getCanonicalName().equals(ActiveMQTextMessage.class.getCanonicalName())) {
                    System.out.println("     - TopicListener: TextMessage '" + ((TextMessage) message).getText());
                } else if (message.getClass().getCanonicalName().equals(ActiveMQMapMessage.class.getCanonicalName())) {
                    System.out.println("     - TopicListener: MapMessage");
                    MapMessage mapMsg = ((MapMessage) message);

                    @SuppressWarnings("unchecked")
                    Enumeration<String> mapKeys = (Enumeration<String>) mapMsg.getMapNames();
                    String key = null;

                    while (mapKeys.hasMoreElements()) {
                        key = mapKeys.nextElement();
                        System.out.println("       + " + key + ": " + mapMsg.getObject(key));
                    }
                }

            } catch (Exception ex) {
                System.err.println("# TopicListener error: " + ex.getMessage());
            }
        }
    }
}
