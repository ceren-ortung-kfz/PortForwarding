package portforwarding;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ClientThread extends Thread {
    private Socket clientSocket;
    private HashMap<Integer, String> destinations;
    private ArrayList<Socket> sockets;
    private HashMap<String, InputStream> serverInputStreams;
    private HashMap<String, OutputStream> serverOutputStreams;

    private boolean forwardingActive = false;

    public ClientThread(Socket clientSocket, HashMap<Integer, String> destinations) {
        this.clientSocket = clientSocket;
        this.destinations = destinations;
        sockets = new ArrayList<>();
        serverInputStreams = new HashMap<>();
        serverOutputStreams = new HashMap<>();
    }

    @Override
    public void run() {
        System.out.println("Started client thread...");

        InputStream clientIn;
        OutputStream clientOut;

        InputStream serverIn;
        OutputStream serverOut;

        try {

            clientIn = clientSocket.getInputStream();
            clientOut = clientSocket.getOutputStream();

            clientSocket.setKeepAlive(true);

            for (Map.Entry entry: destinations.entrySet()) {

                System.out.println(entry.getValue() + ":" + entry.getKey());
                Socket serverSocket = new Socket(entry.getValue().toString(), (int)entry.getKey());
                serverSocket.setKeepAlive(true);

                serverIn = serverSocket.getInputStream();
                serverInputStreams.put(entry.getKey().toString(), serverIn);

                serverOut = serverSocket.getOutputStream();
                serverOutputStreams.put(entry.getKey().toString(), serverOut);

                sockets.add(serverSocket);
            }

            ClientForwardThread clientForward = new ClientForwardThread(this, clientIn, serverOutputStreams);
            clientForward.start();

            ServerForwardThread serverForward = new ServerForwardThread(this, serverInputStreams, clientOut);
            serverForward.start();
        }
        catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    public synchronized void closeConnection() {

    }
}
