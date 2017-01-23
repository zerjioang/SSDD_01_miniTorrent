package es.deusto.ssdd.client.udp.model;

import bittorrent.udp.*;
import bittorrent.udp.Error;
import es.deusto.ssdd.client.udp.client.PeerClient;

/**
 * Created by .local on 15/01/2017.
 */
public enum TrackerResponseParser {

    CONNECTION_RESPONSE {
        @Override
        public BitTorrentUDPMessage parse(byte[] byteArray) {
            return ConnectResponse.parse(byteArray);
        }

        @Override
        protected boolean validate(BitTorrentUDPMessage parsedRequestMessage) {
            return parsedRequestMessage.getAction() == BitTorrentUDPMessage.Action.CONNECT &&
                    parsedRequestMessage.getBytes().length >= 16;
        }

        @Override
        protected byte[] getRequest(BitTorrentUDPMessage parsedRequestMessage) {
            return null;
        }

        @Override
        protected void triggerOnReceiveEvent(PeerClient peerClient, BitTorrentUDPMessage parsedRequestMessage) {
            System.out.println("Connection response message received");
            ConnectResponse response = (ConnectResponse) parsedRequestMessage;
            peerClient.setConnectionResponse(response);
            System.out.println("Peer new id: "+response.getConnectionId());
            //se guarda el nuevo connection id generado por el tracker
            //pasado 1 minuto, se actualizará automaticamente.
            peerClient.setPeerId(response.getTransactionId());
            peerClient.setNeedToConnect(false);
        }

        @Override
        protected byte[] getErrorMessage(boolean valid, BitTorrentUDPMessage parsedRequestMessage) {
            if(parsedRequestMessage.getClass().getSimpleName().equals("Error")){
                Error errorResponse = (Error) parsedRequestMessage;
                System.err.println("PEER CLIENT: "+errorResponse.getMessage());
            }
            else{
                System.err.println("PEER CLIENT: Unexpected message type received");
            }
            return null;
        }
    },
    ANNOUNCE_RESPONSE {
        @Override
        public BitTorrentUDPMessage parse(byte[] byteArray) {
            return AnnounceResponse.parse(byteArray);
        }

        @Override
        protected boolean validate(BitTorrentUDPMessage parsedRequestMessage) {
            return parsedRequestMessage.getAction() == BitTorrentUDPMessage.Action.ANNOUNCE &&
                    parsedRequestMessage.getBytes().length >= 16;
        }

        @Override
        protected byte[] getRequest(BitTorrentUDPMessage parsedRequestMessage) {
            return new byte[0];
        }

        @Override
        protected void triggerOnReceiveEvent(PeerClient peerClient, BitTorrentUDPMessage parsedRequestMessage) {
            System.out.println("Annouce response message received");
            peerClient.setNeedToAnnounce(false);
        }

        @Override
        protected byte[] getErrorMessage(boolean valid, BitTorrentUDPMessage parsedRequestMessage) {
            return new byte[0];
        }
    },
    SCRAPE_RESPONSE {
        @Override
        public BitTorrentUDPMessage parse(byte[] byteArray) {
            return ScrapeResponse.parse(byteArray);
        }

        @Override
        protected boolean validate(BitTorrentUDPMessage parsedRequestMessage) {
            return parsedRequestMessage.getAction() == BitTorrentUDPMessage.Action.SCRAPE;
        }

        @Override
        protected byte[] getRequest(BitTorrentUDPMessage parsedRequestMessage) {
            return new byte[0];
        }

        @Override
        protected void triggerOnReceiveEvent(PeerClient peerClient, BitTorrentUDPMessage parsedRequestMessage) {
            System.out.println("Scrape response message received");
            
        }

        @Override
        protected byte[] getErrorMessage(boolean valid, BitTorrentUDPMessage parsedRequestMessage) {
            return new byte[0];
        }
    },
    ERROR_RESPONSE {
        @Override
        public BitTorrentUDPMessage parse(byte[] byteArray) {
            return Error.parse(byteArray);
        }

        @Override
        protected boolean validate(BitTorrentUDPMessage parsedRequestMessage) {
            return parsedRequestMessage.getAction() == BitTorrentUDPMessage.Action.ERROR;
        }

        @Override
        protected byte[] getRequest(BitTorrentUDPMessage parsedRequestMessage) {
            return new byte[0];
        }

        @Override
        protected void triggerOnReceiveEvent(PeerClient peerClient, BitTorrentUDPMessage parsedRequestMessage) {
            System.out.println("Error response message received");
            Error e = (Error) parsedRequestMessage;
            System.err.println("Error from tracker: " + e.getMessage());
        }

        @Override
        protected byte[] getErrorMessage(boolean valid, BitTorrentUDPMessage parsedRequestMessage) {
            return new byte[0];
        }
    };

    private static final TrackerResponseParser[] list = TrackerResponseParser.values();

    public static BitTorrentUDPMessage parse(int value, byte[] byteArray) {
        return list[value].parse(byteArray);
    }

    public static boolean validate(int value, BitTorrentUDPMessage parsedRequestMessage) {
        return parsedRequestMessage != null && list[value].validate(parsedRequestMessage);
    }

    public static byte[] getRequest(int value, BitTorrentUDPMessage parsedRequestMessage) {
        return list[value].getRequest(parsedRequestMessage);
    }

    public static void triggerOnReceiveEvent(PeerClient peerClient, int value, BitTorrentUDPMessage parsedRequestMessage) {
        if(parsedRequestMessage!=null)
            list[value].triggerOnReceiveEvent(peerClient, parsedRequestMessage);
    }

    public static byte[] getError(int value, boolean valid, BitTorrentUDPMessage parsedRequestMessage) {
        return list[value].getErrorMessage(valid, parsedRequestMessage);
    }

    protected abstract BitTorrentUDPMessage parse(byte[] byteArray);

    protected abstract boolean validate(BitTorrentUDPMessage parsedRequestMessage);

    protected abstract byte[] getRequest(BitTorrentUDPMessage parsedRequestMessage);

    protected abstract void triggerOnReceiveEvent(PeerClient peerClient, BitTorrentUDPMessage parsedRequestMessage);

    protected abstract byte[] getErrorMessage(boolean valid, BitTorrentUDPMessage parsedRequestMessage);
}
