package es.deusto.ssdd.udp.parser;

import bittorrent.udp.*;
import bittorrent.udp.Error;
import es.deusto.ssdd.client.udp.client.PeerClient;
import es.deusto.ssdd.udp.TrackerUDPServer;

/**
 * Created by .local on 15/01/2017.
 */
public enum PeerRequestParser {

    CONNECTION_REQUEST {
        @Override
        public BitTorrentUDPRequestMessage parse(byte[] byteArray) {
            return ConnectRequest.parse(byteArray);
        }

        @Override
        protected boolean validate(TrackerUDPServer trackerUDPServer, BitTorrentUDPRequestMessage parsedRequestMessage) {
            //validate message minimum size
            //connection id = 0x41727101980
            //action = 0
            return parsedRequestMessage.getConnectionId()==Long.decode("0x41727101980") &&
                    parsedRequestMessage.getAction().value() == 0 &&
                    parsedRequestMessage.getBytes().length >= 16;
        }

        @Override
        protected byte[] getResponse(TrackerUDPServer trackerUDPServer, BitTorrentUDPRequestMessage parsedRequestMessage) {
            ConnectResponse response =  new ConnectResponse();
            //transaction id el que envio el peer
            response.setTransactionId(parsedRequestMessage.getTransactionId());
            //connection id random.
            long randomID = PeerClient.getRandomID();
            response.setConnectionId(randomID);
            //save on tracker as validation during 2 minutes
            trackerUDPServer.savePeerIdForValidation(randomID); //el id que se genera aleatoriamente en el peer
            return response.getBytes();
        }

        @Override
        protected void triggerOnReceiveEvent(TrackerUDPServer trackerUDPServer, BitTorrentUDPRequestMessage parsedRequestMessage) {
            System.out.println("Connection request message received");
        }

        @Override
        protected byte[] getErrorMessage(boolean valid, BitTorrentUDPRequestMessage parsedRequestMessage) {
            Error errorResponse = new Error();
            //transaction id el que envio el peer
            errorResponse.setTransactionId(parsedRequestMessage.getTransactionId());
            //message
            if(valid){
                errorResponse.setMessage("EL mensaje recibido es valido pero no se pudo procesar correctamente");
            }
            else{
                errorResponse.setMessage("No se ha detectado un mensaje ConnectionRequest válido");
            }
            return errorResponse.getBytes();
        }
    },
    ANNOUCE_REQUEST {
        @Override
        public BitTorrentUDPRequestMessage parse(byte[] byteArray) {
            return AnnounceRequest.parse(byteArray);
        }

        @Override
        protected boolean validate(TrackerUDPServer trackerUDPServer, BitTorrentUDPRequestMessage parsedRequestMessage) {
            AnnounceRequest announceRequest = (AnnounceRequest) parsedRequestMessage;
            return announceRequest.getBytes().length == 98 &&
                    announceRequest.getAction() == BitTorrentUDPMessage.Action.ANNOUNCE &&
                    trackerUDPServer.isIdStillValid(announceRequest.getConnectionId());
        }

        @Override
        protected byte[] getResponse(TrackerUDPServer trackerUDPServer, BitTorrentUDPRequestMessage parsedRequestMessage) {
            AnnounceResponse announceResponse = new AnnounceResponse();
            return announceResponse.getBytes();
        }

        @Override
        protected void triggerOnReceiveEvent(TrackerUDPServer trackerUDPServer, BitTorrentUDPRequestMessage parsedRequestMessage) {
            System.out.println("Announce request message received");
        }

        @Override
        protected byte[] getErrorMessage(boolean valid, BitTorrentUDPRequestMessage parsedRequestMessage) {
            return new byte[0];
        }
    },
    SCRAPE_RESQUEST {
        @Override
        public BitTorrentUDPRequestMessage parse(byte[] byteArray) {
            return ScrapeRequest.parse(byteArray);
        }

        @Override
        protected boolean validate(TrackerUDPServer trackerUDPServer, BitTorrentUDPRequestMessage parsedRequestMessage) {
            return false;
        }

        @Override
        protected byte[] getResponse(TrackerUDPServer trackerUDPServer, BitTorrentUDPRequestMessage parsedRequestMessage) {
            return new byte[0];
        }

        @Override
        protected void triggerOnReceiveEvent(TrackerUDPServer trackerUDPServer, BitTorrentUDPRequestMessage parsedRequestMessage) {
            System.out.println("Scrape request message received");
        }

        @Override
        protected byte[] getErrorMessage(boolean valid, BitTorrentUDPRequestMessage parsedRequestMessage) {
            return new byte[0];
        }
    };

    protected abstract BitTorrentUDPRequestMessage parse(byte[] byteArray);
    protected abstract boolean validate(TrackerUDPServer trackerUDPServer, BitTorrentUDPRequestMessage parsedRequestMessage);
    protected abstract byte[] getResponse(TrackerUDPServer trackerUDPServer, BitTorrentUDPRequestMessage parsedRequestMessage);
    protected abstract void triggerOnReceiveEvent(TrackerUDPServer trackerUDPServer, BitTorrentUDPRequestMessage parsedRequestMessage);
    protected abstract byte[] getErrorMessage(boolean valid, BitTorrentUDPRequestMessage parsedRequestMessage);

    private static final PeerRequestParser[] list = PeerRequestParser.values();

    public static BitTorrentUDPRequestMessage parse(int value, byte[] byteArray) {
        return list[value].parse(byteArray);
    }

    public static boolean validate(TrackerUDPServer trackerUDPServer, int value, BitTorrentUDPRequestMessage parsedRequestMessage) {
        if(parsedRequestMessage!=null)
            return list[value].validate(trackerUDPServer, parsedRequestMessage);
        return false;
    }
    public static byte[] getResponse(TrackerUDPServer trackerUDPServer, int value, BitTorrentUDPRequestMessage parsedRequestMessage) {
        return list[value].getResponse(trackerUDPServer, parsedRequestMessage);
    }

    public static void triggerOnReceiveEvent(TrackerUDPServer trackerUDPServer, int value, BitTorrentUDPRequestMessage parsedRequestMessage) {
        if(parsedRequestMessage!=null)
            list[value].triggerOnReceiveEvent(trackerUDPServer, parsedRequestMessage);
    }

    public static byte[] getError(int value, boolean valid, BitTorrentUDPRequestMessage parsedRequestMessage) {
        return list[value].getErrorMessage(valid, parsedRequestMessage);
    }
}
