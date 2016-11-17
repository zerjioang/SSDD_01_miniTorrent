package es.deusto.ssdd.code.net.jms.message;

import es.deusto.ssdd.code.net.jms.listener.JMSMessageSender;
import es.deusto.ssdd.code.net.jms.message.wrapper.GoodbyeMessage;
import es.deusto.ssdd.code.net.jms.message.wrapper.HelloMessage;
import es.deusto.ssdd.code.net.jms.message.wrapper.KeepAliveMessage;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * Created by .local on 14/11/2016.
 */
public enum MessageCollection {

    HELLO_WORLD {
        @Override
        public Message getMessage(JMSMessageSender source) throws JMSException {
            return source
                    .getSession()
                    .createObjectMessage(
                            new HelloMessage(
                                    source.getTrackerId()
                            )
                    );
        }
    },
    BYE_BYE {
        @Override
        public Message getMessage(JMSMessageSender source) throws JMSException {
            return source
                    .getSession()
                    .createObjectMessage(
                            new GoodbyeMessage(
                                    source.getTrackerId()
                            )
                    );
        }
    },
    KEEP_ALIVE {
        @Override
        public Message getMessage(JMSMessageSender source) throws JMSException {
            return source
                    .getSession()
                    .createObjectMessage(
                            new KeepAliveMessage(
                                    source.getTrackerId()
                            )
                    );
        }
    };

    public abstract Message getMessage(JMSMessageSender source) throws JMSException;
}
