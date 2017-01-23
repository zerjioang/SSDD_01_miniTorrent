package bittorrent.udp;

import bittorrent.util.StringUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * Offset          Size            	Name            	Value
 * 0               64-bit integer  	connection_id
 * 8               32-bit integer  	action          	2 // scrape
 * 12              32-bit integer  	transaction_id
 * 16 + 20 * n     20-byte string  	info_hash
 * 16 + 20 * N
 */

public class ScrapeRequest extends BitTorrentUDPRequestMessage {

    private List<String> infoHashes;

    public ScrapeRequest() {
        super(Action.SCRAPE);
        this.infoHashes = new ArrayList<>();
    }

    public static ScrapeRequest parse(byte[] byteArray) {
        int infoHashSize = 20;
        int initialSize = 16;

        ByteBuffer bufferData = ByteBuffer.wrap(byteArray);
        ScrapeRequest scrapeRequest = new ScrapeRequest();
        scrapeRequest.setConnectionId(bufferData.getLong(0));
        scrapeRequest.setAction(Action.valueOf(bufferData.getInt(8)));
        scrapeRequest.setTransactionId(bufferData.getInt(12));
        int index = 16;
        boolean error = false;
        for (index = initialSize; index < byteArray.length && !error; index += infoHashSize) {
            byte[] infoHashBytes = new byte[infoHashSize];
            bufferData.position(index);
            bufferData.get(infoHashBytes);
            String infoHash = StringUtils.toHexString(infoHashBytes);
            boolean notEmpty = !infoHash.matches("[0]+");
            if (notEmpty) {
                scrapeRequest.addInfoHash(infoHash);
            } else {
                error = true;
            }
        }
        return scrapeRequest;
    }

    @Override
    public byte[] getBytes() {
        int infoHashSize = 20;
        int initialSize = 16;

        int size = initialSize + infoHashSize * infoHashes.size();

        ByteBuffer byteBuffer = ByteBuffer.allocate(size);
        byteBuffer.order(ByteOrder.BIG_ENDIAN);

        byteBuffer.putLong(0, getConnectionId());
        byteBuffer.putInt(8, getAction().value());
        byteBuffer.putInt(12, getTransactionId());
        int inicio = infoHashSize;
        for (String infoHash : this.infoHashes) {
            byteBuffer.position(inicio);
            byteBuffer.put(infoHash.getBytes());
            inicio += infoHashSize;
        }

        return byteBuffer.array();
    }

    public List<String> getInfoHashes() {
        return infoHashes;
    }

    public void addInfoHash(String infoHash) {
        if (infoHash != null && !infoHash.trim().isEmpty() && !this.infoHashes.contains(infoHash)) {
            this.infoHashes.add(infoHash);
        }
    }
}
