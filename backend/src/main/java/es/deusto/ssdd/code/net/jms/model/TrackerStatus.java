package es.deusto.ssdd.code.net.jms.model;

import java.awt.*;

/**
 * Created by .local on 16/11/2016.
 */
public enum TrackerStatus {

    ONLINE {
        @Override
        public Color getColor() {
            return Color.GREEN;
        }
    },
    OFFLINE {
        @Override
        public Color getColor() {
            return Color.RED;
        }
    };

    public abstract Color getColor();
}
