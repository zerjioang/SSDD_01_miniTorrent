package es.deusto.ssdd.client.gui.view;

import java.awt.event.ActionListener;

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
    };
    public abstract ActionListener event(ClientWindow window);
}
