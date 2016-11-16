package es.deusto.ssdd.code.net.bittorrent.core;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by .local on 08/11/2016.
 */
public class TrackerUtil {

    private static final String NO_MAC_ADDRESS_FOUND = "no:id:fo:un:dd:00";
    private static String OS = System.getProperty("os.name").toLowerCase();

    public static final String getDeviceMacAddress() {
        InetAddress ip;
        try {
            NetworkInterface network = null;
            if (isMac()) {
                network = NetworkInterface.getByName("en1");
            } else {
                ip = InetAddress.getLocalHost();
                //System.out.println("Current IP address : " + ip.getHostAddress());
                network = NetworkInterface.getByInetAddress(ip);
            }
            byte[] mac = network.getHardwareAddress();
            //System.out.print("Current MAC address : ");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }
            return sb.toString();
        } catch (UnknownHostException | SocketException | NullPointerException e) {
        }
        return NO_MAC_ADDRESS_FOUND;
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
        return "unknown";
    }
}
