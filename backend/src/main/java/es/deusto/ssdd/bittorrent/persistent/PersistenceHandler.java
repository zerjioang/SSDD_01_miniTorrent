package es.deusto.ssdd.bittorrent.persistent;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Created by .local on 08/11/2016.
 */
public class PersistenceHandler {

    private static final String DRIVER_NAME = "org.sqlite.JDBC";
    private static final String DATABASE_PATH = "jdbc:sqlite:";
    private boolean loaded;
    private Connection c;

    public PersistenceHandler(String trackerId) {
        System.out.println(trackerId + " loading persistence handler");
        this.loaded = false;
        init(trackerId);
    }

    private void init(String trackerId) {
        c = null;
        try {
            Class.forName(DRIVER_NAME);
            String databaseName = trackerId + ".db";
            c = DriverManager.getConnection(DATABASE_PATH + databaseName);
            System.out.println("Opened database successfully");
            this.loaded = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isLoaded() {
        return loaded;
    }
}
