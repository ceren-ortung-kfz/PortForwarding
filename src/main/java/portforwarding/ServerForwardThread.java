package portforwarding;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.Iterator;
import java.util.Map;

public class ServerForwardThread extends Thread {
    private static final int BUFFER_SIZE = 1024;

    private Map<String, InputStream> inputStreams;
    private OutputStream outputStream;
    private ClientThread clientThread;

    public ServerForwardThread(ClientThread clientThread, Map<String, InputStream> inputStreams,
                               OutputStream outputStream) {
        this.clientThread = clientThread;
        this.inputStreams = inputStreams;
        this.outputStream = outputStream;
    }

    @Override
    public void run() {
        System.out.println("ServerForward thread is starting...");
        boolean sendStatus = false;
        byte[] buffer = new byte[BUFFER_SIZE];
        int byteReads;

        while (true) {
            try {
                Iterator it = inputStreams.entrySet().iterator();
                while (it.hasNext()) {
                    try {
                        Map.Entry entry = (Map.Entry) it.next();

                        InputStream inInputStream = (InputStream) entry.getValue();
                        byteReads = inInputStream.read(buffer);

                        if (byteReads == -1) {
                            it.remove();
                            break;
                        }

                        if (!sendStatus) {
                            outputStream.write(buffer, 0, byteReads);
                            outputStream.flush();
                            sendStatus = true;
                        }
                    } catch (SocketException se) {

                        if (se.getMessage().equalsIgnoreCase("socket closed")) {
                            System.err.println("Socket closed...");
                            inputStreams.clear();
                            break;
                        }

                        System.err.println("Server input stream read error");
                    }
                }

                sendStatus = false;

                if (!clientThread.forwardingStatus()) {
                    break;
                }
            }
            catch (IOException ioe) {
                System.err.println("Connection is broken server");
                break;
            }
        }
    }
}
