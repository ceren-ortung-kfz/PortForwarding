package portforwarding;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.Map;

public class ServerForwardThread extends Thread {
    private static final int BUFFER_SIZE = 1024;

    private Map<String, InputStream> inputStreams;
    private OutputStream outputStream;

    public ServerForwardThread(Map<String, InputStream> inputStreams,
                               OutputStream outputStream) {
        this.inputStreams = inputStreams;
        this.outputStream = outputStream;
    }

    @Override
    public void run() {
        System.out.println("Forward thread is starting...");
        boolean sendStatus = false;
        byte[] buffer = new byte[BUFFER_SIZE];
        int byteReads;

        try {
            while (true) {
                for (Map.Entry entry: inputStreams.entrySet()) {
                    try {
                        InputStream inInputStream = (InputStream) entry.getValue();
                        byteReads = inInputStream.read(buffer);
                        if (byteReads == -1) {
                            break;
                        }

                        if (!sendStatus) {
                            outputStream.write(buffer, 0, byteReads);
                            outputStream.flush();
                            sendStatus = true;
                        }
                    }
                    catch (SocketException se) {

                        if (se.getMessage().equalsIgnoreCase("socket closed")) {
                            System.err.println("Socket closed...");
                            inputStreams.clear();
                            break;
                        }

                        System.err.println("Server input stream read error");
                    }
                }

                sendStatus = false;
            }
        }
        catch (IOException ioe) {
            System.err.println("Connection is broken server");
        }
    }
}
