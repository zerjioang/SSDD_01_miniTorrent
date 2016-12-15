package es.deusto.ssdd.jms.message;

import es.deusto.ssdd.jms.TrackerInstance;
import es.deusto.ssdd.jms.listener.JMSMessageSender;
import es.deusto.ssdd.jms.message.wrapper.BinaryMessage;
import es.deusto.ssdd.jms.message.wrapper.GoodbyeMessage;
import es.deusto.ssdd.jms.message.wrapper.HelloMessage;
import es.deusto.ssdd.jms.message.wrapper.KeepAliveMessage;

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
    },
    DATABASE_CLONE {
        @Override
        public Message getMessage(JMSMessageSender source) throws JMSException {
            return source
                    .getSession()
                    .createObjectMessage(
                            new BinaryMessage(
                                    source.getTracker().getTrackerId(),
                                    this.getRemoteNode().getTrackerId(),
                                    source.getTracker().getDatabaseArray()
                            )
                    );
        }
    };

    private TrackerInstance remoteNode;

    public abstract Message getMessage(JMSMessageSender source) throws JMSException;

    public TrackerInstance getRemoteNode() {
        return remoteNode;
    }

    public void setRemoteNode(TrackerInstance remoteNode) {
        this.remoteNode = remoteNode;
    }
}
