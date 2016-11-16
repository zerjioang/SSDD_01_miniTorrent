package es.deusto.ssdd.code.net.gui.view;

import es.deusto.ssdd.code.net.jms.TrackerInstance;
import es.deusto.ssdd.code.net.jms.model.TrackerStatus;

import javax.swing.*;
import java.awt.event.ActionListener;

import static javax.swing.JOptionPane.showConfirmDialog;

/**
 * Created by .local
 */
public enum TrackerGUIEvents {
    MENU_CONFIGURE_TRACKER {
        @Override
        public ActionListener event(TrackerWindow window) {
            return actionEvent -> {
                System.out.println("Configure tracker event detected");
            };
        }
    }, MENU_ABOUT {
        @Override
        public ActionListener event(TrackerWindow window) {
            return actionEvent -> {
                System.out.println("menu about event detected");
            };
        }
    }, MENU_EXIT {
        @Override
        public ActionListener event(TrackerWindow window) {
            return actionEvent -> {
                System.out.println("Menu exit event detected");
            };
        }
    }, MENU_FORCE_STOP {
        @Override
        public ActionListener event(TrackerWindow window) {
            return actionEvent -> {
                System.out.println("Menu force stop event detected");
                TrackerInstance instance = window.getInstance();
                if(instance!=null){
                    if(instance.isMaster()){
                        int result = JOptionPane.showConfirmDialog(window, "¿Estas seguro de que quieres parar el tracker Master?", "Forzar parada", JOptionPane.INFORMATION_MESSAGE);
                        executeOnResult(window, result);
                    }
                    else{
                        int result = showConfirmDialog(window, "¿Estas seguro de que quieres parar el tracker Master?", "Forzar parada", JOptionPane.INFORMATION_MESSAGE);
                        executeOnResult(window, result);
                    }
                }
            };
        }

        private void executeOnResult(TrackerWindow window, int result) {
            if(result == JOptionPane.OK_OPTION){
                window.getInstance().stopNode();
                window.updateTrackerStatus(TrackerStatus.OFFLINE);
            }
        }
    };

    public abstract ActionListener event(TrackerWindow window);
}
