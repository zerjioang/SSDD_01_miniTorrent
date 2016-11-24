package es.deusto.ssdd.gui.view;

import es.deusto.ssdd.jms.TrackerInstance;
import es.deusto.ssdd.jms.model.TrackerInstanceNodeType;
import es.deusto.ssdd.jms.model.TrackerStatus;

import java.util.HashMap;

/**
 * Created by .local on 16/11/2016.
 */
public interface InterfaceRefresher {

    void updateNodeType(TrackerInstanceNodeType nodeType);

    void updateTrackerStatus(TrackerStatus status);

    void addTrackerNodeToTable(HashMap<String, TrackerInstance> remoteNode);
}
