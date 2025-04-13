package ca.brocku.logistics;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;


public class Main {

    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        final LogisticsApplication application;
        try {
            application = new LogisticsApplication("amqp://cole:corbett@132.145.102.107:5672");
            System.out.println(("Application started successfully."));
        } catch (IOException | TimeoutException e) {
            logger.error("Failed to start LogisticsApplication: {}", e.getMessage(), e);
            return;
        } catch (URISyntaxException | NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }
}