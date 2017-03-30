package portforwarding;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientThread extends Thread {
    private Socket clientSocket;
    private Map<Integer, String> destinations;
    private List<Socket> sockets;
    private Map<String, InputStream> serverInputStreams;
    private Map<String, OutputStream> serverOutputStreams;
    private ClientForwardThread clientForward;
    private ServerForwardThread serverForward;

    public ClientThread(Socket clientSocket, Map<Integer, String> destinations) {
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

            clientForward = new ClientForwardThread(this, clientIn, serverOutputStreams);
            clientForward.start();

            serverForward = new ServerForwardThread(serverInputStreams, clientOut);
            serverForward.start();
        }
        catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    public synchronized void closeConnection() {

        System.out.println(Thread.activeCount());

        clientForward.interrupt();
        serverForward.interrupt();

        System.out.println(Thread.activeCount());

        try {
            for (Socket socket: sockets) {
                socket.close();
            }
            sockets.clear();
        }
        catch (IOException ie) {
            System.err.println("Server socket close error...");
            ie.printStackTrace();
        }

        try {
            clientSocket.close();
            System.out.println("Closed client connection...");
        }
        catch (IOException ie) {
            System.err.println("Client close error...");
            ie.printStackTrace();
        }
    }
}
