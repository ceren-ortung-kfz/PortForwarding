package portforwarding;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

public class ClientForwardThread extends Thread {
    private static final int BUFFER_SIZE = 1024;

    private InputStream inputStream;
    private Map<String, OutputStream> outputStreams;
    private ClientThread clientThread;

    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private static String stdout;

    public ClientForwardThread(ClientThread clientThread, InputStream inputStream,
                               Map<String, OutputStream> outputStreams) {
        this.clientThread = clientThread;
        this.inputStream = inputStream;
        this.outputStreams = outputStreams;
    }

    @Override
    public void interrupt() {
        super.interrupt();

        stdout = "Interrupted ClientForward thread...";
        System.out.println(stdout);
        logger.info(stdout);

        for(Map.Entry<String, OutputStream> entry : outputStreams.entrySet()) {
            try {
                OutputStream os = entry.getValue();
                os.close();

                inputStream.close();
            }
            catch (IOException ie) {
                logger.severe(ie.getMessage());
                ie.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        stdout = "ClientForward thread is starting...";
        System.out.println(stdout);
        logger.info(stdout);
        byte[] buffer = new byte[BUFFER_SIZE];
        int byteReads;

        try {
            while (true) {
                byteReads = inputStream.read(buffer);
                if (byteReads == -1) {
                    break;
                }

                Iterator it = outputStreams.entrySet().iterator();

                while (it.hasNext()) {
                    Map.Entry entry = (Map.Entry)it.next();

                    try {
                        OutputStream outputStream = (OutputStream) entry.getValue();
                        outputStream.write(buffer, 0, byteReads);
                        outputStream.flush();
                    }
                    catch (SocketException se) {
                        it.remove();
                        stdout = String.format("Outputstream write error port: %s", entry.getKey());
                        System.err.println(stdout);
                        logger.severe(stdout);

                        if (outputStreams.size() < 1) {
                            Thread.currentThread().interrupt();
                            clientThread.closeConnection();
                        }
                    }
                }
            }
        }
        catch (IOException ioe) {
            stdout = "Connection is broken client";
            System.err.println(stdout);
            logger.severe(stdout);
        }

        clientThread.closeConnection();
    }
}
