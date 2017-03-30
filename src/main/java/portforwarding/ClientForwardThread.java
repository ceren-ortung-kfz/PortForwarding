package portforwarding;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.Map;

public class ClientForwardThread extends Thread {
    private static final int BUFFER_SIZE = 1024;

    private InputStream inputStream;
    private Map<String, OutputStream> outputStreams;
    private ClientThread clientThread;

    public ClientForwardThread(ClientThread clientThread, InputStream inputStream,
                               Map<String, OutputStream> outputStreams) {
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

                for (Map.Entry entry : outputStreams.entrySet()) {
                    try {
                        OutputStream outputStream = (OutputStream) entry.getValue();
                        outputStream.write(buffer, 0, byteReads);
                        outputStream.flush();
                    }
                    catch (SocketException se) {
                        System.err.println("Outputstream write error port: " + entry.getKey().toString());
                    }
                }
            }
        }
        catch (IOException ioe) {
            System.err.println("Connection is broken client");
        }

        clientThread.closeConnection();
    }
}
