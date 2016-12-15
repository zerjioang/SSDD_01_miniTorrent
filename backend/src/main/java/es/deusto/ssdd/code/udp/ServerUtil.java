package es.deusto.ssdd.code.udp;

/**
 * Created by .local on 15/12/2016.
 */
public class ServerUtil {

    /**
     *
     * @return devuelve el nombre del sistema operativo
     */
    public static String getOS(){
        return System.getProperty("os.name").toLowerCase();
    }

    /**
     *
     * @return devuelve true si el S.O es mac os. Falso si no lo es
     */
    public static boolean isMac(){
        return getOS().contains("mac os");
    }

    /**
     *
     * @return devuelve true si el S.O es Microsoft Windows. Falso si no lo es
     */
    public static boolean isWin(){
        return getOS().contains("windows");
    }
}