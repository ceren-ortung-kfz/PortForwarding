package portforwarding;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class ClientForwardThread extends Thread {
    private static final int BUFFER_SIZE = 1024;

    private InputStream inputStream;
    private HashMap<String, OutputStream> outputStreams;
    private ClientThread clientThread;

    public ClientForwardThread(ClientThread clientThread, InputStream inputStream,
                               HashMap<String, OutputStream> outputStreams) {
        this.clientThread = clientThread;
        this.inputStream = inputStream;
        this.outputStreams = outputStreams;
    }

    @Override
    public void run() {
        System.out.println("ClientForward thread is starting...");
        byte[] buffer = new byte[BUFFER_SIZE];
        int byteReads;

        try {
            while (true) {
                byteReads = inputStream.read(buffer);
                if (byteReads == -1) {
                    break;
                }

                for (Map.Entry entry: outputStreams.entrySet()) {
                    OutputStream outputStream = (OutputStream)entry.getValue();
                    outputStream.write(buffer, 0, byteReads);
                    outputStream.flush();
                }
            }
        }
        catch (IOException ioe) {
            System.err.println("Connection is broken");
        }

        clientThread.closeConnection();
    }
}
