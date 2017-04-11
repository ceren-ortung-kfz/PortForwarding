package portforwarding;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

public class ServerForwardThread extends Thread {
    private static final int BUFFER_SIZE = 1024;

    private Map<String, InputStream> inputStreams;
    private OutputStream outputStream;
    private ClientThread clientThread;

    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private static String stdout;

    public ServerForwardThread(ClientThread clientThread, Map<String, InputStream> inputStreams,
                               OutputStream outputStream) {
        this.clientThread = clientThread;
        this.inputStreams = inputStreams;
        this.outputStream = outputStream;
    }

    @Override
    public void interrupt() {
        super.interrupt();

        stdout = "Interrupted ServerForward thread...";
        System.out.println(stdout);
        logger.info(stdout);

        for(Map.Entry<String, InputStream> entry : inputStreams.entrySet()) {
            try {
                InputStream is = entry.getValue();
                is.close();

                outputStream.close();
            }
            catch (IOException ie) {
                logger.severe(ie.getMessage());
                ie.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        stdout = "ServerForward thread is starting...";
        System.out.println(stdout);
        logger.info(stdout);
        boolean sendStatus = false;
        byte[] buffer;
        int byteReads;

        while (true) {
            try {
                Iterator it = inputStreams.entrySet().iterator();
                while (it.hasNext()) {
                    try {
                        buffer = new byte[BUFFER_SIZE];
                        Map.Entry entry = (Map.Entry) it.next();

                        InputStream inInputStream = (InputStream) entry.getValue();
                        byteReads = inInputStream.read(buffer);

                        if (byteReads == -1) {
                            it.remove();
                            break;
                        }

                        if (!sendStatus) {
                            System.out.println(entry.getKey());
                            outputStream.write(buffer, 0, byteReads);
                            outputStream.flush();
                            sendStatus = true;
                        }
                    } catch (SocketException se) {

                        if (se.getMessage().equalsIgnoreCase("socket closed")) {
                            stdout = "Socket closed...";
                            System.out.println(stdout);
                            logger.severe(stdout);
                            inputStreams.clear();
                            break;
                        }

                        stdout = "Server input stream read error";
                        System.err.println(stdout);
                        logger.severe(stdout);
                    }
                }

                sendStatus = false;

                if (!clientThread.forwardingStatus()) {
                    break;
                }
            }
            catch (IOException ioe) {
                stdout = "Connection is broken server";
                System.err.println(stdout);
                logger.severe(stdout);
                break;
            }
        }
    }
}
