package es.deusto.ssdd.code.udp;

import es.deusto.ssdd.code.udp.protocol.Protocol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Created by .local on 15/12/2016.
 */
public class ClientRequestParser {

    private String response;
    private String clientRequest;
    private SocketManager sockManager;
    private TrackerUDPServer ms;

    public ClientRequestParser(SocketManager sockManager, TrackerUDPServer ms) {
        response = "";
        this.sockManager = sockManager;
        this.ms = ms;
    }

    public synchronized void setClientRequest(String requestLine) {
        clientRequest = requestLine;
    }

    public synchronized void parse() {
        ArrayList<String> listaComando = parseComando();

        String comando = listaComando.get(0);
        switch (comando) {
            case Protocol.EXIT:
                // codigo que se ejecutara al recibir una peticion EXIT desde el cliente
                //TODO exit
                break;
            default:
                response = "-1 Se ha detectado una accion no valida";
        }
    }

    private synchronized ArrayList<String> parseComando() {
        ArrayList<String> lista = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(clientRequest,Protocol.SEPARATOR);
        while (st.hasMoreTokens()) {
            lista.add(st.nextToken());
        }
        return lista;
    }

    public synchronized byte[] getResponse() {
        return (response + "\n").getBytes();
    }

    public void responder() throws IOException {

        byte[] response = this.getResponse();

        // la respuesta ya incluye CLRF
        System.out.println("[Respuesta a enviar] " + new String(response));
        sockManager.write(response, response.length);

        // poner el socket en espera para que reciba nuevas ordenes
        boolean clientAlive = true;

        while ((clientRequest = sockManager.read()) != null && clientAlive) {
            System.out.println("[Datos recibidos] " + clientRequest);
            this.setClientRequest(clientRequest);
            this.parse();
            response = this.getResponse();
            if (response != null) {
                System.out.println("[Respuesta a enviar] "+new String(response));
                System.out.println("Request  " + clientRequest);
                sockManager.write(response, response.length);
                //exit if needed
                if (clientRequest.equals(Protocol.EXIT)) {
                    String ip = sockManager.getMySocket().getLocalAddress().toString().substring(1);
                    System.out.println("Cerrando conexion con el cliente"+ ip);
                    // parar de ejecutar este hilo en el servidor
                    clientAlive = false;
                }
            } else {
                System.err.println("La respuesta del servidor era nula");
            }
        }
    }
}