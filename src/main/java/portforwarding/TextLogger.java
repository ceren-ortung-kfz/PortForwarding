package portforwarding;

import java.io.IOException;
import java.util.logging.*;

public class TextLogger {
    static private FileHandler file;
    static private String fileName = "port-forwarding-log.xml";

    static public void setup() throws IOException {
        Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

        Logger rootLogger = Logger.getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        if(handlers[0] instanceof ConsoleHandler){
            rootLogger.removeHandler(handlers[0]);
        }

        logger.setLevel(Level.INFO);

        file = new FileHandler(fileName);
        logger.addHandler(file);
    }
}
