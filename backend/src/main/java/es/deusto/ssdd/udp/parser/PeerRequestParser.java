package es.deusto.ssdd.udp.parser;

import bittorrent.udp.*;
import bittorrent.udp.Error;
import es.deusto.ssdd.bittorrent.core.SwarmInfo;
import es.deusto.ssdd.client.udp.client.PeerClient;
import es.deusto.ssdd.jms.TrackerInstance;
import es.deusto.ssdd.udp.TrackerUDPServer;

import java.net.InetAddress;

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
            return parsedRequestMessage.getConnectionId() == Long.decode("0x41727101980") &&
                    parsedRequestMessage.getAction() == BitTorrentUDPMessage.Action.CONNECT &&
                    parsedRequestMessage.getBytes().length >= 16;
        }

        @Override
        protected byte[] getResponse(TrackerUDPServer trackerUDPServer, BitTorrentUDPRequestMessage parsedRequestMessage) {
            ConnectResponse response = new ConnectResponse();
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
        protected void triggerOnReceiveEvent(TrackerUDPServer trackerUDPServer, InetAddress clientAddress, int clientPort, BitTorrentUDPRequestMessage parsedRequestMessage) {
            trackerUDPServer.addLogLine("debug: Connection request message received");
        }

        @Override
        protected byte[] getErrorMessage(boolean valid, BitTorrentUDPRequestMessage parsedRequestMessage, String customMessage) {
            return geneateGenericMessage(parsedRequestMessage, customMessage);
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
                    trackerUDPServer.isConnectionIdStillValid(announceRequest.getConnectionId());
        }

        @Override
        protected byte[] getResponse(TrackerUDPServer trackerUDPServer, BitTorrentUDPRequestMessage parsedRequestMessage) {
            TrackerInstance tracker = trackerUDPServer.getTracker();
            AnnounceRequest requestMesage = (AnnounceRequest) parsedRequestMessage;
            AnnounceResponse announceResponse = new AnnounceResponse();
            //transaction id el que envio el peer
            announceResponse.setTransactionId(parsedRequestMessage.getTransactionId());
            //add peer info of that given file. search by infohash string
            SwarmInfo info = tracker.findAnnounceInfoOf(requestMesage.getHexInfoHash());
            if (info != null) {
                announceResponse.setInterval(info.getInterval());
                announceResponse.setLeechers(info.getLeechers());
                announceResponse.setSeeders(info.getSeeders());
                announceResponse.setPeers(info.getPeers());
                return announceResponse.getBytes();
            }
            return getErrorMessage(true, parsedRequestMessage, "No hay peers disponibles para el torrent solicitado");
        }

        @Override
        protected void triggerOnReceiveEvent(TrackerUDPServer trackerUDPServer, InetAddress clientAddress, int clientPort, BitTorrentUDPRequestMessage parsedRequestMessage) {
            trackerUDPServer.addLogLine("debug: Announce request message received");
            //save peer <-> torrent relation as part of swarm data on tracker
            AnnounceRequest request = (AnnounceRequest) parsedRequestMessage;
            trackerUDPServer.addPeerToSwarm(clientAddress, clientPort, request);
        }

        @Override
        protected byte[] getErrorMessage(boolean valid, BitTorrentUDPRequestMessage parsedRequestMessage, String customMessage) {
            return geneateGenericMessage(parsedRequestMessage, customMessage);
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
        protected void triggerOnReceiveEvent(TrackerUDPServer trackerUDPServer, InetAddress clientAddress, int clientPort, BitTorrentUDPRequestMessage parsedRequestMessage) {
            trackerUDPServer.addLogLine("debug: Scrape request message received");
        }

        @Override
        protected byte[] getErrorMessage(boolean valid, BitTorrentUDPRequestMessage parsedRequestMessage, String customMessage) {
            return geneateGenericMessage(parsedRequestMessage, customMessage);
        }
    };

    private static final PeerRequestParser[] list = PeerRequestParser.values();

    private static byte[] geneateGenericMessage(BitTorrentUDPRequestMessage parsedRequestMessage, String customMessage) {
        Error errorResponse = new Error();
        //transaction id el que envio el peer
        errorResponse.setTransactionId(parsedRequestMessage.getTransactionId());
        //custom message detailing error
        errorResponse.setMessage(customMessage);
        return errorResponse.getBytes();
    }

    public static BitTorrentUDPRequestMessage parse(int value, byte[] byteArray) {
        return list[value].parse(byteArray);
    }

    public static boolean validate(TrackerUDPServer trackerUDPServer, int value, BitTorrentUDPRequestMessage parsedRequestMessage) {
        if (parsedRequestMessage != null)
            return list[value].validate(trackerUDPServer, parsedRequestMessage);
        return false;
    }

    public static byte[] getResponse(TrackerUDPServer trackerUDPServer, int value, BitTorrentUDPRequestMessage parsedRequestMessage) {
        return list[value].getResponse(trackerUDPServer, parsedRequestMessage);
    }

    public static void triggerOnReceiveEvent(TrackerUDPServer trackerUDPServer, int value, InetAddress clientAddress, int clientPort, BitTorrentUDPRequestMessage parsedRequestMessage) {
        if (parsedRequestMessage != null)
            list[value].triggerOnReceiveEvent(trackerUDPServer, clientAddress, clientPort, parsedRequestMessage);
    }

    public static byte[] getError(int value, boolean valid, BitTorrentUDPRequestMessage parsedRequestMessage, String customMessage) {
        return list[value].getErrorMessage(valid, parsedRequestMessage, customMessage);
    }

    protected abstract BitTorrentUDPRequestMessage parse(byte[] byteArray);

    protected abstract boolean validate(TrackerUDPServer trackerUDPServer, BitTorrentUDPRequestMessage parsedRequestMessage);

    protected abstract byte[] getResponse(TrackerUDPServer trackerUDPServer, BitTorrentUDPRequestMessage parsedRequestMessage);

    protected abstract void triggerOnReceiveEvent(TrackerUDPServer trackerUDPServer, InetAddress clientAddress, int clientPort, BitTorrentUDPRequestMessage parsedRequestMessage);

    protected abstract byte[] getErrorMessage(boolean valid, BitTorrentUDPRequestMessage parsedRequestMessage, String customMessage);
}
