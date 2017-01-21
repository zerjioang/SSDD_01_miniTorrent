package es.deusto.ssdd.bittorrent.core;

import bittorrent.udp.PeerInfo;

import java.util.List;

public class SwarmInfo {

    private int seeders, leechers, interval;
    private List<PeerInfo> peers;

    public SwarmInfo(int seeders, int leechers, List<PeerInfo> peers) {
        this.seeders = seeders;
        this.leechers = leechers;
        this.peers = peers;
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
}
