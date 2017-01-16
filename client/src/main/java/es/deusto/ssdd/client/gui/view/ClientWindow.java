package es.deusto.ssdd.client.gui.view;


import es.deusto.ssdd.client.udp.client.PeerClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ClientWindow extends JFrame {

    private final PeerClient client;
    private final JTable activeTrackersTable;
    private DefaultTableModel activeTrackersTableModel;

    /**
     * Create the frame.
     */
    public ClientWindow(PeerClient client) {
        this.client = client;

        //set system theme
        setSystemTheme();

        setTitle("Torrent client");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setBounds(100, 100, 450, 300);
        setMinimumSize(new Dimension(800, 400));

        JPanel contentPane = createTopBar();

        JPanel panelCentre = new JPanel();
        contentPane.add(panelCentre, BorderLayout.CENTER);
        panelCentre.setLayout(new BorderLayout(0, 0));

        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        panelCentre.add(tabbedPane);

        JPanel panel = new JPanel();
        tabbedPane.addTab("Active torrents", null, panel, null);
        panel.setLayout(new BorderLayout(0, 0));

        JScrollPane tableScrollPane = new JScrollPane();
        panel.add(tableScrollPane, BorderLayout.CENTER);

        activeTrackersTable = new JTable();
        activeTrackersTableModel = new DefaultTableModel();
        activeTrackersTableModel.setColumnIdentifiers(
                new String[]{
                        "Torrent name", "Downloaded", "Uploaded", "Ratio", "Total size"
                }
        );
        activeTrackersTable.setModel(activeTrackersTableModel);
        activeTrackersTableModel.addColumn(new String[]{
                "demo.pdf", "0kb", "0kb", "-", "1 Mb"
        });
        activeTrackersTable.repaint();
        tableScrollPane.setViewportView(activeTrackersTable);

        JPanel panelBottom = new JPanel();
        contentPane.add(panelBottom, BorderLayout.SOUTH);

        JLabel lblClientsConnected = new JLabel("Peers connected:");
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

        //set window centered
        setLocationRelativeTo(null);
    }

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                ClientWindow frame = new ClientWindow(null);
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
        mntmExit.addActionListener(ClientWindowEvents.MENU_EXIT.event(this));
        mnFile.add(mntmExit);

        JMenu mnSettings = new JMenu("Settings");
        menuBar.add(mnSettings);

        JMenuItem mntmConfigurarTracker = new JMenuItem("Torrent settings");
        mntmConfigurarTracker.addActionListener(ClientWindowEvents.MENU_CONFIGURE_TORRENT_CLIENT.event(this));
        mnSettings.add(mntmConfigurarTracker);

        JMenu mnHelp = new JMenu("Help");
        menuBar.add(mnHelp);

        JMenuItem mntmAbout = new JMenuItem("About");
        mntmAbout.addActionListener(ClientWindowEvents.MENU_ABOUT.event(this));
        mnHelp.add(mntmAbout);
        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);
        return contentPane;
    }

    private ClientWindow getThisWindow() {
        return this;
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
