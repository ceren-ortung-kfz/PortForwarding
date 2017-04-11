package portforwarding;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PortForwarding {

    private static final String REGEX = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]" +
            "|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";

    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public static void main(String[] args) throws IOException {

        String stdout;

        TextLogger.setup();

        if (args.length < 2) {
            stdout = "Invalid usage. it should be like <port no> <client ip:client port>...";
            System.out.println(stdout);
            logger.warning(stdout);
            System.exit(1);
        }

        stdout = "Port forwarding is starting...";
        System.out.println(stdout);
        logger.info(stdout);
        int sourcePort = Integer.valueOf(args[0]);

        List<String> destinations = new ArrayList<>();

        int num;
        Pattern pattern;
        for(num = 1; num < args.length; num++) {
            pattern = Pattern.compile(REGEX);
            String[] target = args[num].split(":");

            Matcher matcher = pattern.matcher(target[0]);

            if (matcher.find()) {
                stdout = String.format("Added: %s:%s", target[0], target[1]);
                System.out.println(stdout);
                logger.info(stdout);
                destinations.add(target[0] + ":" + target[1]);
            }
            else {
                stdout = String.format("Invalid ip adress: %s", args[num]);
                System.err.println(stdout);
                logger.warning(stdout);
            }
        }

        if (destinations.size() < 1 ) {
            stdout = "Not found host(s) to forwarding...";
            System.err.println(stdout);
            logger.warning(stdout);
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
                stdout = "Server closed...";
                System.err.println(stdout);
                logger.severe(stdout);
                break;
            }
        }
    }
}
    