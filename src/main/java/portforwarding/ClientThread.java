package portforwarding;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Map;
import java.util.List;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

public class ClientThread extends Thread {
    private Socket clientSocket;
    private List<String> destinations;
    private List<Socket> sockets;
    private Map<String, InputStream> serverInputStreams;
    private Map<String, OutputStream> serverOutputStreams;
    private ClientForwardThread clientForward;
    private ServerForwardThread serverForward;
    private boolean forwardingActive = false;

    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private static String stdout;

    public ClientThread(Socket clientSocket, List<String> destinations) {
        this.clientSocket = clientSocket;
        this.destinations = destinations;
        sockets = new ArrayList<>();
        serverInputStreams = new TreeMap<>();
        serverOutputStreams = new HashMap<>();
    }

    @Override
    public void run() {
        stdout = "Started client thread...";
        System.out.println(stdout);
        logger.info(stdout);

        InputStream clientIn;
        OutputStream clientOut;

        InputStream serverIn;
        OutputStream serverOut;

        try {

            clientIn = clientSocket.getInputStream();
            clientOut = clientSocket.getOutputStream();

            clientSocket.setKeepAlive(true);

            int i;
            String destination;
            for (i=0; i < destinations.size(); i++) {

                destination = destinations.get(i);
                String[] ip_port_arr = destination.split(":");

                stdout = String.format("Connecting to %s", destination);
                System.out.println(stdout);
                logger.info(stdout);

                try {
                    Socket serverSocket = new Socket(ip_port_arr[0], Integer.valueOf(ip_port_arr[1]));
                    serverSocket.setKeepAlive(true);

                    serverIn = serverSocket.getInputStream();
                    serverInputStreams.put(i + "_" + destination, serverIn);

                    serverOut = serverSocket.getOutputStream();
                    serverOutputStreams.put(i + "_" + destination, serverOut);

                    sockets.add(serverSocket);

                    stdout = "Connected";
                    System.out.println(stdout);
                    logger.info(stdout);
                }
                catch (ConnectException ce) {
                    clientSocket.close();

                    stdout = String.format("Connection failed: %s", destination);
                    System.err.println(stdout);
                    logger.severe(stdout);
                    System.exit(1);
                }
            }

            forwardingActive = true;

            clientForward = new ClientForwardThread(this, clientIn, serverOutputStreams);
            clientForward.start();

            serverForward = new ServerForwardThread(this, serverInputStreams, clientOut);
            serverForward.start();
        }
        catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    public synchronized void closeConnection() {

        clientForward.interrupt();
        serverForward.interrupt();

        forwardingActive = false;

        try {
            for (Socket socket: sockets) {
                socket.close();
            }
            sockets.clear();
        }
        catch (IOException ie) {
            stdout = "Server socket close error...";
            System.err.println(stdout);
            logger.severe(stdout);
            ie.printStackTrace();
        }

        try {
            clientSocket.close();
            stdout = "Closed client connection...";
            System.out.println(stdout);
            logger.info(stdout);
        }
        catch (IOException ie) {
            stdout = "Client close error...";
            System.err.println(stdout);
            logger.severe(stdout);
            ie.printStackTrace();
        }

        this.interrupt();
    }

    public synchronized boolean forwardingStatus() {
        return forwardingActive;
    }
}
