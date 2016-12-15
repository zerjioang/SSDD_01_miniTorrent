package es.deusto.ssdd.code.udp;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by .local on 15/12/2016.
 */
public class SocketManager {
    private Socket mySocket;

    private DataOutputStream bufferEscritura;
    private BufferedReader bufferLectura;

    public SocketManager(Socket sock) throws IOException {
        this.mySocket = sock;
        startStreams();
    }

    /**
     *
     * @param address
     *            InetAddress
     * @param port
     *            int numero de puerto
     * @throws IOException
     */
    public SocketManager(InetAddress address, int port) throws IOException {
        mySocket = new Socket(address, port);
        startStreams();
    }

    /**
     *
     * @param host
     *            String nombre del servidor al que se conecta
     * @param port
     *            int puerto de conexion
     * @throws IOException
     */
    public SocketManager(String host, int port) throws IOException {
        mySocket = new Socket(host, port);
        startStreams();
    }

    /**
     * Inicializaci�n de los bufferes de lectura y escritura del socket
     *
     * @throws IOException
     */
    public synchronized void startStreams() throws IOException {
        bufferEscritura = new DataOutputStream(mySocket.getOutputStream());
        bufferLectura = new BufferedReader(new InputStreamReader(
                mySocket.getInputStream()));
    }

    public synchronized void closeStreams() throws IOException {
        bufferEscritura.close();
        bufferLectura.close();
    }

    public synchronized void closeSocket() throws IOException {
        mySocket.close();
    }

    /**
     *
     * @return String
     * @throws IOException
     */
    public synchronized String read() throws IOException {
        return (bufferLectura.readLine());
    }

    public synchronized void write(String contenido) throws IOException {
        bufferEscritura.writeBytes(contenido);
    }

    public synchronized void write(byte[] buffer, int bytes)
            throws IOException {
        bufferEscritura.write(buffer, 0, bytes);
    }

    /**
     * @return the mySocket
     */
    public synchronized Socket getMySocket() {
        return mySocket;
    }

    /**
     * @param mySocket
     *            the mySocket to set
     */
    public synchronized void setMySocket(Socket mySocket) {
        this.mySocket = mySocket;
    }

    @Override
    public String toString() {
        return getName();
    }

    public String getName() {
        return getMySocket().getRemoteSocketAddress().toString();
    }

    public DataOutputStream getBufferEscritura() {
        return bufferEscritura;
    }

    public void setBufferEscritura(DataOutputStream bufferEscritura) {
        this.bufferEscritura = bufferEscritura;
    }

    public BufferedReader getBufferLectura() {
        return bufferLectura;
    }

    public void setBufferLectura(BufferedReader bufferLectura) {
        this.bufferLectura = bufferLectura;
    }
}