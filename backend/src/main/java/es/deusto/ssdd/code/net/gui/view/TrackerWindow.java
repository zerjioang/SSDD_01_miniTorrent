package es.deusto.ssdd.code.net.gui.view;

import es.deusto.ssdd.code.net.jms.TrackerInstance;
import es.deusto.ssdd.code.net.jms.model.TrackerInstanceNodeType;
import es.deusto.ssdd.code.net.jms.model.TrackerStatus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TrackerWindow extends JFrame implements InterfaceRefresher, WindowFocusListener {

    private final TrackerInstance instance;
    private final JLabel labelTrackerIp;
    private final JLabel labelTrackerPort;
    private final JLabel labelTrackerId;
    private final JLabel labelTrackerOnline;
    private final JTable activeTrackersTable;
    private DefaultTableModel activeTrackersTableModel;

    /**
     * Create the frame.
     */
    public TrackerWindow(TrackerInstance instance) {
        this.instance = instance;

        //set system theme
        setSystemTheme();

        setTitle("Admin window");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 450, 300);
        setMinimumSize(new Dimension(800, 400));

        JPanel contentPane = createTopBar();

        JPanel panelTop = new JPanel();
        contentPane.add(panelTop, BorderLayout.NORTH);

        JLabel lblTrackerIp = new JLabel("Tracker IP:");
        panelTop.add(lblTrackerIp);

        labelTrackerIp = new JLabel("127.0.0.1");
        panelTop.add(labelTrackerIp);

        JLabel lblTrackerPort = new JLabel("Tracker port:");
        panelTop.add(lblTrackerPort);

        labelTrackerPort = new JLabel("1234");
        panelTop.add(labelTrackerPort);

        JLabel lblTrackerId = new JLabel("Tracker ID:");
        panelTop.add(lblTrackerId);

        labelTrackerId = new JLabel("4F");
        panelTop.add(labelTrackerId);

        JLabel lblStatus = new JLabel("Status:");
        panelTop.add(lblStatus);

        labelTrackerOnline = new JLabel("Online");
        panelTop.add(labelTrackerOnline);

        JPanel panelCentre = new JPanel();
        contentPane.add(panelCentre, BorderLayout.CENTER);
        panelCentre.setLayout(new BorderLayout(0, 0));

        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        panelCentre.add(tabbedPane);

        JPanel panel = new JPanel();
        tabbedPane.addTab("Active tracker", null, panel, null);
        panel.setLayout(new BorderLayout(0, 0));

        JScrollPane scrollPane_1 = new JScrollPane();
        panel.add(scrollPane_1, BorderLayout.CENTER);

        activeTrackersTable = new JTable();
        activeTrackersTableModel = new DefaultTableModel();
        activeTrackersTableModel.setColumnIdentifiers(
                new String[]{
                        "ID", "Tracker IP", "Tracker port", "Node type", "Last keep alive"
                }
        );
        activeTrackersTable.setModel(activeTrackersTableModel);
        scrollPane_1.setViewportView(activeTrackersTable);

        JPanel panel_1 = new JPanel();
        tabbedPane.addTab("Active Swarms", null, panel_1, null);
        panel_1.setLayout(new BorderLayout(0, 0));

        JScrollPane scrollPane = new JScrollPane();
        panel_1.add(scrollPane, BorderLayout.CENTER);

        JTable table_1 = new JTable();
        table_1.setModel(new DefaultTableModel(
                new Object[][]{
                        {null, null, null, null, null, null, null, null, null},
                        {null, null, null, null, null, null, null, null, null}
                },
                new String[]{
                        "Filename", "Extension", "Size", "Seeders", "Leechers", "Peer list", "Downloaded", "Total", "Progress"
                }
        ));
        scrollPane.setViewportView(table_1);

        JPanel panelBottom = new JPanel();
        contentPane.add(panelBottom, BorderLayout.SOUTH);

        JLabel lblClientsConnected = new JLabel("Clients connected:");
        panelBottom.add(lblClientsConnected);

        JLabel label_2 = new JLabel("0");
        panelBottom.add(label_2);

        JLabel lblSwarms = new JLabel("Swarms:");
        panelBottom.add(lblSwarms);

        JLabel label_3 = new JLabel("0");
        panelBottom.add(label_3);

        JLabel lblSharedFiles = new JLabel("Shared files:");
        panelBottom.add(lblSharedFiles);

        JLabel label_4 = new JLabel("0");
        panelBottom.add(label_4);

        //populate window with data
        populateWindow();

        //set window centered
        setLocationRelativeTo(null);

        //
        addWindowFocusListener(this);
    }

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                TrackerWindow frame = new TrackerWindow(null);
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private JPanel createTopBar() {
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu mnFile = new JMenu("File");
        menuBar.add(mnFile);

        JMenuItem mntmExit = new JMenuItem("Exit");
        mntmExit.addActionListener(TrackerGUIEvents.MENU_EXIT.event(this));
        mnFile.add(mntmExit);

        JMenu mnSettings = new JMenu("Settings");
        menuBar.add(mnSettings);

        JMenuItem mntmConfigurarTracker = new JMenuItem("Tracker settings");
        mntmConfigurarTracker.addActionListener(TrackerGUIEvents.MENU_CONFIGURE_TRACKER.event(this));
        mnSettings.add(mntmConfigurarTracker);

        JMenuItem mntmForceStop = new JMenuItem("Force stop");
        mntmForceStop.addActionListener(TrackerGUIEvents.MENU_FORCE_STOP.event(this));
        mnSettings.add(mntmForceStop);

        JMenu mnHelp = new JMenu("Help");
        menuBar.add(mnHelp);

        JMenuItem mntmAbout = new JMenuItem("About");
        mntmAbout.addActionListener(TrackerGUIEvents.MENU_ABOUT.event(this));
        mnHelp.add(mntmAbout);
        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);
        return contentPane;
    }

    private void populateWindow() {
        if (instance != null) {
            updateTrackerStatus(instance.getTrackerStatus());
            labelTrackerIp.setText(instance.getIp());
            labelTrackerPort.setText("" + instance.getPort());
            labelTrackerId.setText(instance.getTrackerId());
            updateTrackerStatus(instance.getTrackerStatus());
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

    public TrackerInstance getInstance() {
        return instance;
    }

    public void updateWindowView() {
        this.updateTrackerStatus(instance.getTrackerStatus());
        this.updateNodeType(instance.getNodeType());
    }

    public void updateTrackerStatus(TrackerStatus status) {
        if (status != null) {
            this.labelTrackerOnline.setText("" + status);
            this.labelTrackerOnline.setForeground(status.getColor());
        }
        //empty tracker nodes tables info
        emptyActiveTrackersTable();
        this.repaint();
    }

    @Override
    public void addTrackerNodeToTable(HashMap<String, TrackerInstance> remoteNodeList) {
        // add row dynamically into the table
        //columns: "ID", "Tracker IP", "Tracker port", "Node type", "Last keep alive"
        try {
            emptyActiveTrackersTable();

            Iterator it = remoteNodeList.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                TrackerInstance remoteNode = (TrackerInstance) pair.getValue();
                activeTrackersTableModel.addRow(
                        new Object[]{
                                remoteNode.getTrackerId(), remoteNode.getIp(), remoteNode.getPort(), remoteNode.getNodeType().toString(), remoteNode.getLastKeepAlive()
                        }
                );
                it.remove();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void emptyActiveTrackersTable() {
        while (activeTrackersTableModel.getRowCount() != 0) {
            activeTrackersTableModel.removeRow(0);
        }
    }

    @Override
    public void updateNodeType(TrackerInstanceNodeType nodeType) {
        this.setTitle("Tracker Node :: " + instance.getNodeType());
        this.repaint();
    }

    @Override
    public void windowGainedFocus(WindowEvent windowEvent) {
        this.updateWindowView();
    }

    @Override
    public void windowLostFocus(WindowEvent windowEvent) {

    }
}
