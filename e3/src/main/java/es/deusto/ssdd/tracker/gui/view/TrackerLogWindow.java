package es.deusto.ssdd.tracker.gui.view;

/**
 * Created by .local on 21/01/2017.
 */

import es.deusto.ssdd.tracker.gui.model.CellRenderer;
import es.deusto.ssdd.tracker.gui.observer.TorrentObserver;
import es.deusto.ssdd.tracker.jms.TrackerInstance;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class TrackerLogWindow extends JFrame implements TorrentObserver{

    private DefaultTableModel model;
    private TrackerInstance instance;
    private JPanel contentPane;
    private JTable table;

    public TrackerLogWindow(TrackerInstance instance) {
        this.instance = instance;
        init();
    }

    private void init() {
        //set system theme
        setSystemTheme();
        setLogo();
        setTrackerTitle();

        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setBounds(100, 100, 550, 800);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        JScrollPane scrollPane = new JScrollPane();
        contentPane.add(scrollPane, BorderLayout.CENTER);

        table = new JTable();
        model = new DefaultTableModel(
                new Object[][]{}, //zero rows
                new String[]{
                        "Log" // 1 column called Log
                }
        );
        table.setModel(model);
        customizeTable();
        scrollPane.setViewportView(table);
        this.setType(Type.POPUP);
    }

    private void customizeTable() {
        //set cell renderer
        table.setDefaultRenderer(Object.class, new CellRenderer());

        //hide separation lines
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
    }

    private void setLogo() {
        //set icon
        ClassLoader classLoader = TrackerLogWindow.class.getClassLoader();
        setIconImage(Toolkit.getDefaultToolkit().getImage(classLoader.getResource("img/logo.png")));
    }

    private void setTrackerTitle() {
        String id = instance.getTrackerId();
        if(id!=null){
            this.setTitle("Log ["+id+"]");
        }
        else{
            this.setTitle("Log");
        }
    }

    private void setSystemTheme() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException e) {
            System.err.println(e.getLocalizedMessage());
        }
    }

    public synchronized void addLogLine(String data) {
        if (data != null) {
            this.model.addRow(new String[]{data});
        }
    }

    @Override
    public void update() {
        if(instance!=null){
            setTrackerTitle();
        }
    }
}

