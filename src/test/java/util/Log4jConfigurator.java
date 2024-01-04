package util;

import java.io.File;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Log4jConfigurator {

    private static final Logger LOGGER = Logger.getLogger(Log4jConfigurator.class);

    public static void configure() {
        try {
            String log4jConfigFile = System.getProperty("user.dir") + File.separator + Constants.log4jConfigFile;
            PropertyConfigurator.configure(log4jConfigFile);
            LOGGER.info("Log4j configuration loaded successfully.");
        } catch (Exception e) {
            LOGGER.error("Error configuring Log4j: " + e.getMessage());
        }
    }

    public static Logger getLogger() {
        return LOGGER;
    }
}
