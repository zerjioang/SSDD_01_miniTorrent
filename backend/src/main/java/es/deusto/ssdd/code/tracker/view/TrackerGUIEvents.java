package es.deusto.ssdd.code.tracker.view;

import java.awt.event.ActionEvent;

/**
 * Created by .local on 18/10/2016.
 */
public enum TrackerGUIEvents {
    MENU_CONFIGURE_TRACKER {
        @Override
        public void event(ActionEvent event) {
            System.out.println("Configure tracker event detected");
        }
    }, MENU_ABOUT {
        @Override
        public void event(ActionEvent event) {
            System.out.println("menu about event detected");
        }
    }, MENU_EXIT {
        @Override
        public void event(ActionEvent event) {
            System.out.println("Menu exit event detected");
        }
    }, MENU_FORCE_STOP {
        @Override
        public void event(ActionEvent event) {
            System.out.println("Menu force stop event detected");
        }
    };

    public abstract void event(ActionEvent event);
}
