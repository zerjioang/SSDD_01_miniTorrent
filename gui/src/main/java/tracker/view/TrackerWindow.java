package tracker.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class TrackerWindow extends JFrame {

    /**
     * Create the frame.
     */
    private TrackerWindow() {

        //set system theme
        setSystemTheme();

        setTitle("Tracker Admin");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 450, 300);
        setMinimumSize(new Dimension(800, 400));

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu mnFile = new JMenu("File");
        menuBar.add(mnFile);

        JMenuItem mntmExit = new JMenuItem("Exit");
        mntmExit.addActionListener(TrackerGUIEvents.MENU_EXIT::event);
        mnFile.add(mntmExit);

        JMenu mnSettings = new JMenu("Settings");
        menuBar.add(mnSettings);

        JMenuItem mntmConfigurarTracker = new JMenuItem("Tracker settings");
        mntmConfigurarTracker.addActionListener(TrackerGUIEvents.MENU_CONFIGURE_TRACKER::event);
        mnSettings.add(mntmConfigurarTracker);

        JMenuItem mntmForceStop = new JMenuItem("Force stop");
        mntmForceStop.addActionListener(TrackerGUIEvents.MENU_FORCE_STOP::event);
        mnSettings.add(mntmForceStop);

        JMenu mnHelp = new JMenu("Help");
        menuBar.add(mnHelp);

        JMenuItem mntmAbout = new JMenuItem("About");
        mntmAbout.addActionListener(TrackerGUIEvents.MENU_ABOUT::event);
        mnHelp.add(mntmAbout);
        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        JPanel panelTop = new JPanel();
        contentPane.add(panelTop, BorderLayout.NORTH);

        JLabel lblTrackerIp = new JLabel("Tracker IP:");
        panelTop.add(lblTrackerIp);

        JLabel label = new JLabel("127.0.0.1");
        panelTop.add(label);

        JLabel lblTrackerPort = new JLabel("Tracker port:");
        panelTop.add(lblTrackerPort);

        JLabel label_1 = new JLabel("1234");
        panelTop.add(label_1);

        JLabel lblTrackerId = new JLabel("Tracker ID:");
        panelTop.add(lblTrackerId);

        JLabel lblf = new JLabel("4F");
        panelTop.add(lblf);

        JLabel lblStatus = new JLabel("Status:");
        panelTop.add(lblStatus);

        JLabel lblOnline = new JLabel("Online");
        panelTop.add(lblOnline);

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

        JTable table = new JTable();
        table.setModel(new DefaultTableModel(
                new Object[][]{
                        {null, null, null, null, null},
                        {null, null, null, null, null},
                        {null, null, null, null, null},
                },
                new String[]{
                        "ID", "Tracker IP", "Tracker port", "Node type", "Last keep alive"
                }
        ));
        scrollPane_1.setViewportView(table);

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
    }

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                TrackerWindow frame = new TrackerWindow();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void setSystemTheme() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException e) {
            System.err.println(e.getLocalizedMessage());
        }
    }

}
