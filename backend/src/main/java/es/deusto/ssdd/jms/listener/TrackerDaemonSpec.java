package es.deusto.ssdd.jms.listener;

/**
 * Created by .local on 16/11/2016.
 */
public enum TrackerDaemonSpec {

    HANDSHAKE_SERVICE {
        @Override
        public String getServiceName() {
            return "tracker.handshake";
        }

    },
    KEEP_ALIVE_SERVICE {
        @Override
        public String getServiceName() {
            return "tracker.alive";
        }

    },
    DATA_SYNC_SERVICE {
        @Override
        public String getServiceName() {
            return "tracker.sync";
        }
    };

    public abstract String getServiceName();
}
