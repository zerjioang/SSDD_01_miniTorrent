package es.deusto.ssdd.tracker.gui.observer;

/**
 * Created by .local on 21/01/2017.
 */
public interface TorrentObservable {

    public void addObserver(TorrentObserver o);

    public void removeObserver(TorrentObserver o);

    public void notifyObserver();
}
