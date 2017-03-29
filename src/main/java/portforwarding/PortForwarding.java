package portforwarding;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class PortForwarding {

    private static final int SOURCE_PORT = 10040;
    private static final String DESTINATION_HOST = "127.0.0.1";
    private static final int DESTINATION_PORT = 10041;

    private static final String DESTINATION_HOST_2 = "127.0.0.1";
    private static final int DESTINATION_PORT_2 = 10042;

    public static void main(String[] args) throws IOException {
        System.out.println("Port forwarding is starting...");

        ServerSocket serverSocket = new ServerSocket(SOURCE_PORT);

        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();

                ClientThread clientThread = new ClientThread(clientSocket, DESTINATION_HOST, DESTINATION_PORT);
                clientThread.start();

                ClientThread clientThread2 = new ClientThread(clientSocket, DESTINATION_HOST_2, DESTINATION_PORT_2);
                clientThread2.start();

            }
            catch (Exception err) {
                System.err.println("Server closed...");
                break;
            }
        }
    }
}

