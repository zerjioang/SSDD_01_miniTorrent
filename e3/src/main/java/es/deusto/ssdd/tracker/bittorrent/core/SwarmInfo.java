package es.deusto.ssdd.tracker.bittorrent.core;

import bittorrent.udp.PeerInfo;
import es.deusto.ssdd.client.udp.model.SharingFile;
import es.deusto.ssdd.tracker.jms.TrackerInstance;

import java.util.List;

public class SwarmInfo {

    private String owner;
    private int seeders;
    private int leechers;
    private int interval;
    private int timesDownloaded;

    private List<PeerInfo> peers;
    private transient SharingFile file;

    public SwarmInfo(TrackerInstance instance) {
        this.owner = instance.getTrackerId();
    }

    public int getSeeders() {
        return seeders;
    }

    public void setSeeders(int seeders) {
        this.seeders = seeders;
    }

    public int getLeechers() {
        return leechers;
    }

    public void setLeechers(int leechers) {
        this.leechers = leechers;
    }

    public List<PeerInfo> getPeers() {
        return peers;
    }

    public void setPeers(List<PeerInfo> peers) {
        this.peers = peers;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public SharingFile getFile() {
        return file;
    }

    public void setFile(SharingFile file) {
        this.file = file;
    }

    public int getTimesDownloaded() {
        return timesDownloaded;
    }

    public void setTimesDownloaded(int timesDownloaded) {
        this.timesDownloaded = timesDownloaded;
    }

    public void addPeer(PeerInfo peerInfo) {
        if (peerInfo != null) {
            this.peers.add(peerInfo);
        }
    }

    public void increaseLeechersBy(int i) {
        this.leechers += i;
    }

    public void increaseSeedersBy(int i) {
        this.seeders += i;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
