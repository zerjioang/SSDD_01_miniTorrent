package es.deusto.ssdd.tracker.gui.model;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Created by .local on 21/01/2017.
 */
public class CellRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (value != null) {
            //TODO replace by interface
            String data = (String) value;
            data = data.toLowerCase();
            if (data.startsWith("error:")) {
                c.setForeground(Color.RED);
            } else if (data.startsWith("debug:")) {
                c.setForeground(Color.YELLOW);
            } else if (data.startsWith("stream:")) {
                c.setForeground(Color.GREEN);
            } else if (data.startsWith("persistence:")) {
                c.setForeground(Color.orange);
            } else {
                c.setForeground(Color.WHITE);
            }
            c.setBackground(Color.BLACK);
        }
        return c;
    }
}
