package es.deusto.ssdd.tracker.gui.view;

import es.deusto.ssdd.tracker.gui.observer.TorrentObserver;
import es.deusto.ssdd.tracker.jms.TrackerInstance;
import es.deusto.ssdd.tracker.jms.model.TrackerInstanceNodeType;
import es.deusto.ssdd.tracker.jms.model.TrackerStatus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TrackerWindow extends JFrame implements TorrentObserver {

    private final TrackerInstance instance;
    private final JLabel labelTrackerIp;
    private final JLabel labelTrackerPort;
    private final JLabel labelTrackerId;
    private final JLabel labelTrackerOnline;
    private final JTable activeTrackersTable;
    private final JLabel lblClientCount;
    private final JLabel lblSwarmCount;
    private final JLabel lblBytesShared;
    private DefaultTableModel activeTrackersTableModel;

    private TrackerLogWindow logWindow;

    /**
     * Create the frame.
     */
    public TrackerWindow(TrackerInstance instance) {
        this.instance = instance;

        //set window logo
        setLogo();
        //set system theme
        setSystemTheme();
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setBounds(100, 100, 450, 300);
        setMinimumSize(new Dimension(800, 400));

        JPanel contentPane = createTopBar();

        JPanel panelTop = new JPanel();
        contentPane.add(panelTop, BorderLayout.NORTH);

        JLabel lblTrackerIp = new JLabel("Tracker IP:");
        panelTop.add(lblTrackerIp);

        labelTrackerIp = new JLabel("127.0.0.1");
        panelTop.add(labelTrackerIp);

        JLabel lblTrackerPort = new JLabel("Tracker UDP port:");
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

        lblClientCount = new JLabel("0");
        panelBottom.add(lblClientCount);

        JLabel lblSwarms = new JLabel("Swarms:");
        panelBottom.add(lblSwarms);

        lblSwarmCount = new JLabel("0");
        panelBottom.add(lblSwarmCount);

        JLabel lblSharedFiles = new JLabel("Sharing bytes:");
        panelBottom.add(lblSharedFiles);

        lblBytesShared = new JLabel("0");
        panelBottom.add(lblBytesShared);

        //set window centered
        setLocationRelativeTo(null);

        //set log window visible
        this.logWindow = new TrackerLogWindow(instance);
        this.logWindow.setVisible(true);
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

    private void setLogo() {
        //set icon
        ClassLoader classLoader = TrackerWindow.class.getClassLoader();
        setIconImage(Toolkit.getDefaultToolkit().getImage(classLoader.getResource("img/logo.png")));
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

        JMenuItem mntmShowLog = new JMenuItem("Show log window");
        mntmShowLog.addActionListener(TrackerGUIEvents.MENU_SHOW_LOG.event(this));
        mnSettings.add(mntmShowLog);

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

    private void emptyActiveTrackersTable() {
        while (activeTrackersTableModel.getRowCount() != 0) {
            activeTrackersTableModel.removeRow(0);
        }
    }

    private TrackerWindow getThisWindow() {
        return this;
    }

    public void addLogLine(String data) {
        this.logWindow.addLogLine(data);
    }

    public void showLogWindow() {
        if (this.logWindow != null) {
            this.logWindow.setVisible(true);
        }
    }

    @Override
    public void update() {
        //update window title
        if (instance != null) {
            TrackerInstanceNodeType type = instance.getNodeType();
            if (type != null) {
                this.setTitle("Tracker Node :: " + instance.getNodeType());
            } else {
                this.setTitle("Tracker Node");
            }

            //update ip
            labelTrackerIp.setText(instance.getIp());
            //update port
            labelTrackerPort.setText("" + instance.getPort());
            //update tracker id
            labelTrackerId.setText(instance.getTrackerId());

            //update table content
            // add row dynamically into the table
            //columns: "ID", "Tracker IP", "Tracker port", "Node type", "Last keep alive"
            try {
                emptyActiveTrackersTable();
                HashMap<String, TrackerInstance> list = instance.getTrackerNodeList();
                if (list != null) {
                    Iterator it = list.entrySet().iterator();
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
                }
            } catch (Exception e) {
                addLogLine("Error: " + e.getLocalizedMessage());
            }

            //update status
            TrackerStatus status = instance.getTrackerStatus();
            if (status != null) {
                this.labelTrackerOnline.setText(status.name());
                this.labelTrackerOnline.setForeground(status.getColor());
            }

            //update clients connected
            int clientCount = instance.getClientCount();
            this.lblClientCount.setText(String.valueOf(clientCount));
            //update swarm count
            int swarmCount = instance.getSwarmCount();
            this.lblSwarmCount.setText(String.valueOf(swarmCount));
            //update shared files bytes
            long shared = instance.getSharingBytesCount();
            this.lblBytesShared.setText(String.valueOf(shared));
        }
    }

    public TrackerLogWindow getLogWindow() {
        return logWindow;
    }
}
