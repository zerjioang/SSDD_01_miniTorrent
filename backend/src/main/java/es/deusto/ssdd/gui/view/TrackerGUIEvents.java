package es.deusto.ssdd.gui.view;

import es.deusto.ssdd.jms.TrackerInstance;

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
                window.addLogLine("Configure tracker event detected");
            };
        }
    }, MENU_ABOUT {
        @Override
        public ActionListener event(TrackerWindow window) {
            return actionEvent -> {
                window.addLogLine("menu about event detected");
            };
        }
    }, MENU_EXIT {
        @Override
        public ActionListener event(TrackerWindow window) {
            return actionEvent -> {
                window.addLogLine("Menu exit event detected");
                forceTrackerStop(window);
                window.dispose();
            };
        }
    }, MENU_FORCE_STOP {
        @Override
        public ActionListener event(TrackerWindow window) {
            return actionEvent -> {
                window.addLogLine("Menu force stop event detected");
                forceTrackerStop(window);
                window.dispose();
            };
        }
    }, MENU_SHOW_LOG {
        @Override
        public ActionListener event(TrackerWindow window) {
            return actionEvent -> {
                window.showLogWindow();
            };
        }
    };

    private static void forceTrackerStop(TrackerWindow window) {
        TrackerInstance instance = window.getInstance();
        if (instance != null) {
            if (instance.isMaster()) {
                int result = JOptionPane.showConfirmDialog(window, "¿Estas seguro de que quieres parar el tracker Master?", "Forzar parada", JOptionPane.INFORMATION_MESSAGE);
                executeOnResult(window, result);
            } else {
                int result = showConfirmDialog(window, "¿Estas seguro de que quieres parar el tracker?", "Forzar parada", JOptionPane.INFORMATION_MESSAGE);
                executeOnResult(window, result);
            }
        }
    }

    private static void executeOnResult(TrackerWindow window, int result) {
        if (result == JOptionPane.OK_OPTION) {
            window.getInstance().sayGoodByeToCluster();
            window.update();
        }
    }

    public abstract ActionListener event(TrackerWindow window);
}
