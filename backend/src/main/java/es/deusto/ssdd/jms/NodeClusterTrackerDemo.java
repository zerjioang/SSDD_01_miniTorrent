package es.deusto.ssdd.jms;


import bittorrent.metainfo.InfoDictionarySingleFile;
import bittorrent.metainfo.MetainfoFile;
import bittorrent.metainfo.handler.MetainfoHandlerSingleFile;
import es.deusto.ssdd.client.udp.client.PeerClient;
import es.deusto.ssdd.client.udp.model.SharingFile;

import java.io.File;

public class NodeClusterTrackerDemo {

    private static final int NODES = 1;

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < NODES; i++) {
            /*
            try {
                TrackerInstance tracker = new TrackerInstance();
                tracker.deploy();
                //tracker udp server
                TrackerUDPServer udpServer = new TrackerUDPServer(tracker);
                udpServer.backgroundDispatch();
            } catch (JMSException e) {
                e.printStackTrace();
            }
             */
            TrackerInstance tracker = new TrackerInstance();

            //se simula un cliente udp
            PeerClient client = new PeerClient(tracker.getIp(), tracker.getPort());
            //leer un archivo de la carpeta resources
            MetainfoHandlerSingleFile handler = new MetainfoHandlerSingleFile();
            //read from resouces
            ClassLoader classLoader = NodeClusterTrackerDemo.class.getClassLoader();
            File file = new File(classLoader.getResource("torrent/ubuntu-16.04.1-desktop-amd64.iso.torrent").getFile());
            handler.parseTorrenFile(file.getPath());
            MetainfoFile<InfoDictionarySingleFile> meta = handler.getMetainfo();
            SharingFile sharingFile = new SharingFile(meta.getInfo().getName(), meta.getInfo().getInfoHash(), meta.getInfo().getLength());
            sharingFile.setMetaInfo(meta);
            client.shareFile(sharingFile);
            client.connect();
        }
    }
}
