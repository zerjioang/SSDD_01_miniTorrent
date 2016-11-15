package es.deusto.ssdd.code.net.bittorrent.persistent;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Created by .local on 08/11/2016.
 */
public class PersistenceHandler {

    private static final String DRIVER_NAME = "org.sqlite.JDBC";
    private static final String DATABASE_PATH = "jdbc:sqlite:test.db";
    private static PersistenceHandler ourInstance = new PersistenceHandler();
    private boolean loaded;
    private Connection c;

    private PersistenceHandler() {
        this.loaded = false;
        init();
    }

    public static PersistenceHandler getInstance() {
        return ourInstance;
    }

    private void init() {
        c = null;
        try {
            Class.forName(DRIVER_NAME);
            c = DriverManager.getConnection(DATABASE_PATH);
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Opened database successfully");
        this.loaded = true;
    }

    public boolean isLoaded() {
        return loaded;
    }
}
