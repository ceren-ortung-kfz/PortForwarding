package portforwarding;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PortForwarding {

    private static final String REGEX = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]" +
            "|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";

    public static void main(String[] args) throws IOException {

        if (args.length < 2) {
            System.out.println("Invalid usage. it should be like <port no> <client ip:client port>...");
            System.exit(1);
        }

        System.out.println("Port forwarding is starting...");
        int sourcePort = Integer.valueOf(args[0]);

        Map<Integer, String> destinations = new HashMap<>();

        int num;
        Pattern pattern;
        for(num = 1; num < args.length; num++) {
            pattern = Pattern.compile(REGEX);
            String[] target = args[num].split(":");

            Matcher matcher = pattern.matcher(target[0]);

            if (matcher.find()) {
                System.out.println("Added: " + target[0] + ":" + target[1]);
                destinations.put(Integer.valueOf(target[1]), target[0]);
            }
            else {
                System.err.println("Invalid ip adress: " + args[num]);
            }
        }

        if (destinations.size() < 1 ) {
            System.err.println("Not found host(s) to forwarding...");
            System.exit(1);
        }

        ServerSocket serverSocket = new ServerSocket(sourcePort);

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

