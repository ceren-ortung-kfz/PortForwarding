package portforwarding;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ClientThread extends Thread {
    private Socket clientSocket;
    private Socket serverSocket;

    private String host;
    private int port;
    private boolean forwardingActive = false;

    public ClientThread(Socket clientSocket, String host, int port) {
        this.clientSocket = clientSocket;
        this.host = host;
        this.port = port;
    }

    @Override
    public void run() {
        System.out.println("Started client thread...");

        InputStream clientIn;
        OutputStream clientOut;

        InputStream serverIn;
        OutputStream serverOut;

        try {
            serverSocket = new Socket(this.host, this.port);

            serverSocket.setKeepAlive(true);
            clientSocket.setKeepAlive(true);

            clientIn = clientSocket.getInputStream();
            clientOut = clientSocket.getOutputStream();

            serverIn = serverSocket.getInputStream();
            serverOut = serverSocket.getOutputStream();
        }
        catch (IOException ioe) {
            System.err.println("Connection failed");
            return;
        }

        forwardingActive = true;
        ForwardThread clientForward = new ForwardThread(this, clientIn, serverOut);
        clientForward.start();

        ForwardThread serverForward = new ForwardThread(this, serverIn, clientOut);
        serverForward.start();
    }

    public synchronized void closeConnection() {
        try {
            serverSocket.close();
        }
        catch (Exception e) {
            System.err.println("Client thread server socket close error");
            e.printStackTrace();
        }

        try {
            clientSocket.close();
        }
        catch (Exception e) {
            System.err.println("Client thread client socket close error");
            e.printStackTrace();
        }

        if (forwardingActive) {
            System.out.println("TCP Forwarding " +
                    clientSocket.getInetAddress().getHostAddress()
                    + ":" + clientSocket.getPort() + " <--> " +
                    serverSocket.getInetAddress().getHostAddress()
                    + ":" + serverSocket.getPort() + " stopped.");
            forwardingActive = false;
        }
    }
}
