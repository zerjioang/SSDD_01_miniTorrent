package es.deusto.ssdd.bittorrent.persistent;

import com.google.gson.Gson;
import es.deusto.ssdd.gui.model.observ.TorrentObserver;
import es.deusto.ssdd.jms.TrackerInstance;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by .local on 08/11/2016.
 */
public class PersistenceHandler implements TorrentObserver {

    private final TrackerInstance tracker;
    private boolean loaded;
    private String databaseName;
    private File storageFile;
    private String lastDatabaseCheckContent;
    private boolean canSaveData;

    public PersistenceHandler(TrackerInstance tracker) {
        this.tracker = tracker;
        tracker.addLogLine("persistence: Starting persistence handler...");
        this.loaded = false;
        this.canSaveData = true;
        this.lastDatabaseCheckContent = "";
        init(tracker.getTrackerId());
    }

    private void init(String trackerId) {
        databaseName = trackerId + ".json";
        storageFile = new File(databaseName);
        tracker.addLogLine("persistence: Opened JSON storage successfully");
        this.loaded = false;
        this.canSaveData = true;
        this.lastDatabaseCheckContent = "";
    }

    public void saveData(Object o) {
        if(o!=null){
            // try-with-resources statement based on post comment below :)
            try {
                FileWriter file = new FileWriter(storageFile.getAbsolutePath());
                BufferedWriter writer = new BufferedWriter(file);
                //block all master can-save-data-request
                this.canSaveData = false;
                String data = new Gson().toJson(o);
                //check if data has changed from previous save
                if(!this.lastDatabaseCheckContent.equals(data)){
                    writer.write(data);
                    tracker.addLogLine("persistence: Writing "+data.length()+ " bytes...");
                    this.lastDatabaseCheckContent = data;
                    writer.close();
                    file.close();
                }
                else{
                    tracker.addLogLine("persistence: no data changes detected on local storage");
                    tracker.addLogLine("persistence: no need to update");
                }
            } catch (IOException e) {
                tracker.addLogLine("error: Error saving local session data.");
                tracker.addLogLine("error: Possible cause: "+e.getLocalizedMessage());
            }
            //disable lock, once file is written on hd
            this.canSaveData = true;
        }
    }

    public void saveData(String data) {
        if(data!=null){
            // try-with-resources statement based on post comment below :)
            try {
                FileWriter file = new FileWriter(storageFile.getAbsolutePath());
                //check if data has changed from previous save
                if(!this.lastDatabaseCheckContent.equals(data)){
                    file.write(data);
                    tracker.addLogLine("persistence: Writing "+data.length()+ " bytes...");
                    this.lastDatabaseCheckContent = data;
                }
                else{
                    tracker.addLogLine("persistence: no data changes detected on local storage");
                    tracker.addLogLine("persistence: no need to update");
                }
            } catch (IOException e) {
                tracker.addLogLine("error: Error saving local session data.");
                tracker.addLogLine("error: Possible cause: "+e.getLocalizedMessage());
            }
        }
    }

    public boolean isLoaded() {
        return loaded;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void overwrite(byte[] data) {
        tracker.addLogLine("persistence: overwritting LOCAL DATABASE with REMOTE");
        try {
            //delete file
            boolean deleted = new File(databaseName).delete();
            if (deleted) {
                tracker.addLogLine("persistence: old database deleted");
                //create new one
                Files.write(Paths.get(databaseName), data);
                tracker.addLogLine("persistence: new database created");
            } else {
                tracker.addLogLine("Error: something happen when deleting. Could not complete Database Overwrite");
            }
        } catch (IOException e) {
            tracker.addLogLine("Error: "+e.getLocalizedMessage());
        }
    }

    public void deleteDatabase() {
        tracker.addLogLine("persistence: DELETE DATABASE");
        //delete file
        boolean deleted = new File(databaseName).delete();
        if (deleted) {
            tracker.addLogLine("persistence: Database file deleted");
        }
        else{
            tracker.addLogLine("error: Database file NOT deleted");
        }
    }

    @Override
    public void update() {
        if(tracker!=null){
            SwarmData persistentInfo = tracker.getSwarmInfo();
            if(persistentInfo!=null){
                tracker.addLogLine("persistence: syncing persistence data...");
                saveData(persistentInfo);
                //notfy that master database has changed to slaves
                tracker.notifyDatabaseHasChanged();
            }
            else{
                tracker.addLogLine("persistence: no information to sync.");
            }
        }
    }

    public byte[] getDatabaseInfoAsArray() {
        try {
            return Files.readAllBytes(
                    Paths.get(
                            this.storageFile.getAbsolutePath()
                    )
            );
        } catch (IOException e) {
            tracker.addLogLine("error: no information to sync.");
            tracker.addLogLine("error: caused by "+e.getLocalizedMessage());
        }
        return null;
    }

    public boolean canSaveData() {
        return this.canSaveData;
    }
}
