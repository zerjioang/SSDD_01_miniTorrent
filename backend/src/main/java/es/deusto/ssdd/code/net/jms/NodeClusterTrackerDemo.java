package es.deusto.ssdd.code.net.jms;


import javax.jms.JMSException;

public class NodeClusterTrackerDemo {

    private static final int NODES = 3;

    public static void main(String[] args) throws Exception {
        for(int i=0; i< NODES; i++){
            try {
                new TrackerInstance().deploy();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }
}
