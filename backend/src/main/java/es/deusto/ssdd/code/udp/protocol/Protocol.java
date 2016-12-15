package es.deusto.ssdd.code.udp.protocol;

/**
 * Created by .local on 15/12/2016.
 */
public class Protocol {

    public static final String EXIT = "EXIT";
    public static final String SEPARATOR = " ";
    private static final String CRLF = "\n";

    public byte[] sendFilename(String filename) {
        return ("filename" + SEPARATOR + filename + CRLF).getBytes();
    }

    public byte[] exit() {
        return (EXIT + CRLF).getBytes();
    }
}