package es.deusto.ssdd.tracker.bittorrent.core;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by .local on 08/11/2016.
 */
public class TrackerUtil {

    private static final String NO_MAC_ADDRESS_FOUND = "no:id:fo:un:dd:00";
    private static final String NO_IP_ADDRESS_FOUND = "unknown";
    private static String OS = System.getProperty("os.name").toLowerCase();

    public static String getDeviceMacAddress() {
        try {
            NetworkInterface network;
            network = getNetworkInterface();
            byte[] mac = network.getHardwareAddress();
            StringBuilder sb = new StringBuilder();
            return getMacString(mac, sb);
        } catch (UnknownHostException | SocketException | NullPointerException e) {
            e.printStackTrace();
        }
        return NO_MAC_ADDRESS_FOUND;
    }

    private static String getMacString(byte[] mac, StringBuilder sb) {
        for (int i = 0; i < mac.length; i++) {
            sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
        }
        return sb.toString();
    }

    private static NetworkInterface getNetworkInterface() throws SocketException, UnknownHostException {
        NetworkInterface network;
        InetAddress ip;
        if (isMac()) {
            network = NetworkInterface.getByName("en1");
        } else {
            ip = InetAddress.getLocalHost();
            network = NetworkInterface.getByInetAddress(ip);
        }
        return network;
    }

    public static boolean isMac() {
        return (OS.contains("mac"));
    }

    public static String getIP() {
        InetAddress ip;
        try {
            ip = InetAddress.getLocalHost();
            return ip.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return NO_IP_ADDRESS_FOUND;
    }
}
