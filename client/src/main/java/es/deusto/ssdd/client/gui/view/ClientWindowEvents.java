package es.deusto.ssdd.client.gui.view;

import es.deusto.ssdd.client.udp.model.SharingFile;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * Created by .local
 */
public enum ClientWindowEvents {
    MENU_CONFIGURE_TORRENT_CLIENT {
        @Override
        public ActionListener event(ClientWindow window) {
            return actionEvent -> {
                System.out.println("Configure tracker event detected");
            };
        }
    }, MENU_ABOUT {
        @Override
        public ActionListener event(ClientWindow window) {
            return actionEvent -> {
                System.out.println("menu about event detected");
                JOptionPane.showMessageDialog(window, "version 1.0\r\nLicense: GPLv3\r\nGithub repo: https://github.com/zerjioang/SSDD_01_miniTorrent/", "About", JOptionPane.INFORMATION_MESSAGE);
            };
        }
    }, MENU_EXIT {
        @Override
        public ActionListener event(ClientWindow window) {
            return actionEvent -> {
                System.out.println("Menu exit event detected");
                window.dispose();
            };
        }
    }, BUTTON_START {
        @Override
        public ActionListener event(ClientWindow window) {
            return actionEvent -> {
                System.out.println("button start event");
            };
        }
    }, BUTTON_PAUSE {
        @Override
        public ActionListener event(ClientWindow window) {
            return actionEvent -> {
                System.out.println("button pause event");
            };
        }
    }, BUTTON_STOP {
        @Override
        public ActionListener event(ClientWindow window) {
            return actionEvent -> {
                System.out.println("button stop event");
            };
        }
    }, MENU_OPEN_TORRENT {
        @Override
        public ActionListener event(ClientWindow window) {
            return actionEvent -> {
                System.out.println("menu open torrent event");
                JFileChooser fileChooser = new JFileChooser();
                int seleccion = fileChooser.showOpenDialog(window);
                if (seleccion == JFileChooser.APPROVE_OPTION) {
                    File ficheroTorrent = fileChooser.getSelectedFile();
                    if (ficheroTorrent != null) {
                        SharingFile shareFile = window.openFile(ficheroTorrent);
                        if (shareFile != null) {
                            int result = JOptionPane.showConfirmDialog(window, "CUrrent torrent will start downloading, please make sure it is correct:\r\n\r\n" + shareFile.getInfoString() + "\r\n\r\nStart downloading?", "Torrent details", JOptionPane.INFORMATION_MESSAGE);
                            if (result == JFileChooser.APPROVE_OPTION)
                                //add file to client and start downloading
                                window.startDownloading(shareFile);
                        }
                    } else {
                        JOptionPane.showMessageDialog(window, "Could not read selected torrent metadata.", "Wrong input", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(window, "Please select a valid torrent file.", "Wrong input", JOptionPane.ERROR_MESSAGE);
                }
            };
        }
    };

    public abstract ActionListener event(ClientWindow window);
}