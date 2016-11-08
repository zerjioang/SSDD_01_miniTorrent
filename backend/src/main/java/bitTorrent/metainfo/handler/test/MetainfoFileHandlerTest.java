package bitTorrent.metainfo.handler.test;

import bitTorrent.metainfo.handler.MetainfoHandler;
import bitTorrent.metainfo.handler.MetainfoHandlerMultipleFile;
import bitTorrent.metainfo.handler.MetainfoHandlerSingleFile;

import java.io.File;

public class MetainfoFileHandlerTest {
    public static void main(String[] args) {
        try {
            File folder = new File("torrent");
            MetainfoHandler<?> handler = null;

            if (folder.isDirectory()) {
                for (File torrent : folder.listFiles()) {
                    try {
                        if (torrent.getPath().contains(".torrent")) {
                            handler = new MetainfoHandlerSingleFile();
                            handler.parseTorrenFile(torrent.getPath());
                        }
                    } catch (Exception ex) {
                        if (torrent.getPath().contains(".torrent")) {
                            handler = new MetainfoHandlerMultipleFile();
                            handler.parseTorrenFile(torrent.getPath());
                        }
                    }

                    if (handler != null) {
                        System.out.println("#######################################\n" + torrent.getPath());
                        System.out.println(handler.getMetainfo());
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("# MetainforFileHandlerTest: " + ex.getMessage());
        }
    }
}