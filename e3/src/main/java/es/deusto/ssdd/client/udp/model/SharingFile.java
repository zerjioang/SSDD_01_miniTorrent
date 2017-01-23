package es.deusto.ssdd.client.udp.model;

import bittorrent.metainfo.InfoDictionarySingleFile;
import bittorrent.metainfo.MetainfoFile;

/**
 * Created by .local on 15/01/2017.
 */
public class SharingFile {

    private String filename;
    private byte[] infohash;
    private long downloadedBytes, leftBytes, uploadedBytes, totalBytes;
    private boolean needToAnnounce;
    private MetainfoFile<InfoDictionarySingleFile> metaInfo;
    private boolean announce;
    private long updateInterval;

    public SharingFile() {
        needToAnnounce = true;
    }

    public SharingFile(String filename, byte[] infohash, long totalBytes) {
        this();
        this.filename = filename;
        this.infohash = infohash;
        this.downloadedBytes = 0;
        this.leftBytes = totalBytes;
        this.uploadedBytes = 0;
        this.totalBytes = totalBytes;
        this.announce = true;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public long getDownloadedBytes() {
        return downloadedBytes;
    }

    public void setDownloadedBytes(long downloadedBytes) {
        this.downloadedBytes = downloadedBytes;
    }

    public long getLeftBytes() {
        return leftBytes;
    }

    public void setLeftBytes(long leftBytes) {
        this.leftBytes = leftBytes;
    }

    public long getUploadedBytes() {
        return uploadedBytes;
    }

    public void setUploadedBytes(long uploadedBytes) {
        this.uploadedBytes = uploadedBytes;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public void setTotalBytes(long totalBytes) {
        this.totalBytes = totalBytes;
    }

    public byte[] getInfohash() {
        return infohash;
    }

    public MetainfoFile<InfoDictionarySingleFile> getMetaInfo() {
        return metaInfo;
    }

    public void setMetaInfo(MetainfoFile<InfoDictionarySingleFile> metaInfo) {
        this.metaInfo = metaInfo;
    }

    public boolean needsToBeAnnounced() {
        return this.announce;
    }

    public void setNeedToAnnounce(boolean needToAnnounce) {
        this.needToAnnounce = needToAnnounce;
    }

    public long getUpdateInterval() {
        return updateInterval;
    }

    public void setUpdateInterval(long updateInterval) {
        this.updateInterval = updateInterval;
    }

    public String getInfoString() {
        return "Filename: " + this.filename + "\r\n" +
                "Total bytes: " + this.totalBytes + "\r\n" +
                "Encoding: " + this.metaInfo.getEncoding() + "\r\n" +
                "Created by: " + this.metaInfo.getCreatedBy() + "\r\n" +
                "MD5: " + this.metaInfo.getInfo().getMd5sum() + "\r\n" +
                "Announce URL: " + this.metaInfo.getAnnounce() + "\r\n";
    }
}
