package es.deusto.ssdd.tracker.jms.model;

/**
 * Created by .local on 15/11/2016.
 */
public enum TrackerInstanceNodeType {

    MASTER {
        @Override
        public String toString() {
            return "MASTER";
        }
    }, SLAVE {
        @Override
        public String toString() {
            return "SLAVE";
        }
    }, NONE {
        @Override
        public String toString() {
            return "NONE";
        }
    };

    public abstract String toString();
}
