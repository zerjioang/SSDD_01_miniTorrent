package es.deusto.ssdd.code.net.jms;

import es.deusto.ssdd.code.net.jms.model.TrackerHello;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * Created by .local on 14/11/2016.
 */
public enum TrackerMessage {

    HELLO_WORLD {
        @Override
        public Message getMessage(JMSSenderDaemon sender) throws JMSException {
            return sender.getSession().createObjectMessage(
                    new TrackerHello(sender.getTrackerId()
                    )
            );
        }
    };

    public abstract Message getMessage(JMSSenderDaemon sender) throws JMSException;
}
