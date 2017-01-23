package es.deusto.ssdd.bittorrent.persistent;

import bittorrent.util.ByteUtils;
import es.deusto.ssdd.bittorrent.core.SwarmInfo;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by .local on 23/01/2017.
 */
public class SwarmData extends HashMap<String, SwarmInfo> implements Serializable{

    private String id;

    public SwarmData(){
        //random id. nothing to do with tracker id
        this.id = ByteUtils.generatePeerId();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
