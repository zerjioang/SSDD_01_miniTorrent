package es.deusto.ssdd.code.net.jms.message;

import es.deusto.ssdd.code.net.jms.listener.JMSMessageSender;
import es.deusto.ssdd.code.net.jms.message.wrapper.TrackerHello;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * Created by .local on 14/11/2016.
 */
public enum MessageCollection {

    HELLO_WORLD {
        @Override
        public Message getMessage(JMSMessageSender sender) throws JMSException {
            return sender
                    .getSession()
                    .createObjectMessage(
                        new TrackerHello(
                                sender.getTrackerId()
                                        )
            );
        }
    };

    public abstract Message getMessage(JMSMessageSender sender) throws JMSException;
}
