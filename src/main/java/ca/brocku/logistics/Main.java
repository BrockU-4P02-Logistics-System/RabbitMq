package ca.brocku.logistics;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.TimeoutException;


public class Main {

    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        final LogisticsApplication application;

        try {
            application = new LogisticsApplication("localhost");
            logger.info("Application started successfully.");
        } catch (IOException | TimeoutException e) {
            logger.error("Failed to start LogisticsApplication: {}", e.getMessage(), e);
            return;
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                application.close();
                logger.info("Application shut down gracefully.");
            } catch (IOException e) {
                logger.error("Error during Application shutdown: {}", e.getMessage(), e);
            }
        }));

        logger.info("Application is running...");
    }
}