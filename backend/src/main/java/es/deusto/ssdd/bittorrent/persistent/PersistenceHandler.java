package es.deusto.ssdd.bittorrent.persistent;

import es.deusto.ssdd.jms.TrackerInstance;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by .local on 08/11/2016.
 */
public class PersistenceHandler {

    private static final String DRIVER_NAME = "org.sqlite.JDBC";
    private static final String DATABASE_PATH = "jdbc:sqlite:";
    private static final byte[] EMPTY_DATA = new byte[0];
    private final TrackerInstance tracker;
    private boolean loaded;
    private Connection connection;
    private String databaseName;

    public PersistenceHandler(TrackerInstance tracker) {
        this.tracker = tracker;
        System.out.println(tracker.getTrackerId() + " loading persistence handler");
        this.loaded = false;
        init(tracker.getTrackerId());
    }

    private void init(String trackerId) {
        connection = null;
        try {
            Class.forName(DRIVER_NAME);
            databaseName = trackerId + ".db";
            connection = DriverManager.getConnection(DATABASE_PATH + databaseName);
            System.out.println("Opened database successfully");
            createModel();
            this.loaded = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createModel() {
        try {
            connection.createStatement().execute("CREATE TABLE SWARM(ID INTEGER PRIMARY KEY AUTOINCREMENT, FILENAME VARCHAR(255), FILESIZE INT, PEER INT);");
            connection.createStatement().execute("CREATE TABLE PEER(ID INTEGER PRIMARY KEY AUTOINCREMENT, IP VARCHAR(100) NOT NULL, PORT INT NOT NULL);");
            connection.createStatement().execute("CREATE TABLE SWARM_PEERS(ID INTEGER PRIMARY KEY, PEER_ID INTEGER NOT NULL, SWARM_ID INTEGER NOT NULL, PENDING_DATA VARCHAR(255), DOWNLOADED_DATA VARCHAR(255), FOREIGN KEY(PEER_ID) REFERENCES PEER(id), FOREIGN KEY(SWARM_ID) REFERENCES SWARM(ID));");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isLoaded() {
        return loaded;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public byte[] getDatabaseArray() {
        try {
            byte[] data = Files.readAllBytes(Paths.get(databaseName));
            return data;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return EMPTY_DATA;
    }

    public void overwrite(byte[] data) {
        System.out.println(tracker.getTrackerId() + " overwritting LOCAL DATABASE with REMOTE");
        try {
            //close connection
            connection.close();
            //delete file
            boolean deleted = new File(databaseName).delete();
            if (deleted) {
                System.out.println(tracker.getTrackerId() + " Old file deleted");
                //create new one
                Files.write(Paths.get(databaseName), data);
                System.out.println(tracker.getTrackerId() + " New file created");
            } else {
                System.out.println(tracker.getTrackerId() + " Something happen when deleting. Could not complete Database Overwrite");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
