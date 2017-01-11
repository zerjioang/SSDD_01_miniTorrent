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

    public void setMetaInfo(MetainfoFile<InfoDictionarySingleFile> metaInfo) {
        this.metaInfo = metaInfo;
    }

    public MetainfoFile<InfoDictionarySingleFile> getMetaInfo() {
        return metaInfo;
    }
}
