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
    private static String trackerId = null;

    public static final String getDeviceMacAddress() {
        InetAddress ip;
        try {
            ip = InetAddress.getLocalHost();
            //System.out.println("Current IP address : " + ip.getHostAddress());
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            byte[] mac = network.getHardwareAddress();
            //System.out.print("Current MAC address : ");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }
            return sb.toString();
        } catch (UnknownHostException | SocketException e) {
            e.printStackTrace();
        }
        return NO_MAC_ADDRESS_FOUND;
    }

    /*
    public static final String getTrackerId() {
        if (trackerId == null) {
            trackerId = getDeviceMacAddress() + ":" + System.nanoTime();
        }
        return trackerId;
    }
    */
}
