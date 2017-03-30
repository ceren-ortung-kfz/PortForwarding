package portforwarding;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class PortForwarding {

    private static final int SOURCE_PORT = 10040;

    public static void main(String[] args) throws IOException {
        System.out.println("Port forwarding is starting...");

        Map<Integer, String> destinations = new HashMap<>();
        destinations.put(10041, "127.0.0.1");
        destinations.put(10042, "127.0.0.1");

        ServerSocket serverSocket = new ServerSocket(SOURCE_PORT);

        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();

                ClientThread clientThread = new ClientThread(clientSocket, destinations);
                clientThread.start();

            }
            catch (Exception err) {
                System.err.println("Server closed...");
                break;
            }
        }
    }
}

