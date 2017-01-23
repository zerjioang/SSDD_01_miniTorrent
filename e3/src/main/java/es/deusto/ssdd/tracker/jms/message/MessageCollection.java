package es.deusto.ssdd.tracker.jms.message;

import es.deusto.ssdd.tracker.jms.TrackerInstance;
import es.deusto.ssdd.tracker.jms.listener.JMSMessageSender;
import es.deusto.ssdd.tracker.jms.message.wrapper.*;

import javax.jms.JMSException;
import javax.jms.Message;

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
                                    source.getTracker().getDatabaseInfoAsArray()
                            )
                    );
        }
    },
    SYNC {
        @Override
        public Message getMessage(JMSMessageSender source) throws JMSException {
            return source
                    .getSession()
                    .createObjectMessage(
                            new DataSyncMessage(
                                    source.getTracker().getTrackerId(),
                                    this.getRemoteNode().getTrackerId()
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
