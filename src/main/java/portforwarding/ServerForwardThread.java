package portforwarding;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class ServerForwardThread extends Thread {
    private static final int BUFFER_SIZE = 1024;

    private HashMap<String, InputStream> inputStreams;
    private OutputStream outputStream;
    private ClientThread clientThread;

    public ServerForwardThread(ClientThread clientThread, HashMap<String, InputStream> inputStreams,
                               OutputStream outputStream) {
        this.clientThread = clientThread;
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
                    InputStream inInputStream = (InputStream)entry.getValue();
                    byteReads = inInputStream.read(buffer);

                    if (byteReads == -1) {
                        break;
                    }

                    if (!sendStatus) {
                        System.out.println(entry.getKey());
                        outputStream.write(buffer, 0, byteReads);
                        outputStream.flush();
                        sendStatus = true;
                    }
                }

                sendStatus = false;
            }
        }
        catch (IOException ioe) {
            System.err.println("Connection is broken");
        }

        clientThread.closeConnection();
    }
}
