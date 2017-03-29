package portforwarding;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ForwardThread extends Thread {
    private static final int BUFFER_SIZE = 1024;

    private InputStream inputStream;
    private OutputStream outputStream;
    private ClientThread clientThread;

    public ForwardThread(ClientThread clientThread, InputStream inputStream, OutputStream outputStream) {
        this.clientThread = clientThread;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[BUFFER_SIZE];
        int byteReads;

        try {
            while (true) {
                byteReads = inputStream.read(buffer);
                if (byteReads == -1) {
                    break;
                }

                outputStream.write(buffer, 0, byteReads);
                outputStream.flush();

                try {
                    Thread.sleep(1000);
                }
                catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }
        catch (IOException ioe) {
            System.err.println("Connection is broken");
        }

        clientThread.closeConnection();
    }
}
