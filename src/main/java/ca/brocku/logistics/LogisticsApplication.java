package ca.brocku.logistics;

import ca.brocku.logistics.algorithm.GeneticAlgorithm;
import ca.brocku.logistics.error.RouteNotFoundException;
import ca.brocku.logistics.error.RouteParseException;
import ca.brocku.logistics.model.Route;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

/**
 * Application for handling heavy computational requests
 *
 * How it works:
 * RabbitMQ will consume the request, then the GA will run and
 * find the optimal route and return it to the consumer. This is designed
 * to be a docker image to deploy on a need basis depending on the load of requests.
 */
public class LogisticsApplication implements Closeable {

    private static final Logger logger = LogManager.getLogger(LogisticsApplication.class);
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    private static final String QUEUE = "logistic-request";

    private final ConnectionFactory connectionFactory;
    private Connection connection;

    public LogisticsApplication(String host) throws IOException, TimeoutException {
        this.connectionFactory = new ConnectionFactory();
        this.connectionFactory.setHost(host);

        try (Connection connection = this.connectionFactory.newConnection()) {
            this.connection = connection;
            final Channel channel = connection.createChannel();
            logger.info("Connected to RabbitMQ at host: {}", host);

               channel.basicConsume(QUEUE, true, (s, delivery) -> {
                final String content = new String(delivery.getBody(), StandardCharsets.UTF_8);
                final AMQP.BasicProperties properties = delivery.getProperties();
                final String to = properties.getReplyTo();
                final String cId = properties.getCorrelationId();

                logger.info("Received message from queue '{}': {}", QUEUE, content);

                try {
                    final Route route = GSON.fromJson(content, Route.class);
                    if (route == null) {
                        logger.error("Failed to parse route from message: {}", content);
                        throw new RouteParseException();
                    }

                    logger.info("Processing route: {}", route);

                    // Implement GA logic integration here
                    final GeneticAlgorithm geneticAlgorithm = new GeneticAlgorithm(route);

                    final AMQP.BasicProperties reply = new AMQP.BasicProperties.Builder()
                            .correlationId(cId)
                            .build();

                    geneticAlgorithm.compute().thenAccept(addresses -> {
                        if (addresses.isEmpty()) {
                            logger.error("No addresses found for route: {}", route);
                            throw new RouteNotFoundException(route);
                        }

                        final String response = GSON.toJson(addresses);
                        logger.info("Found optimal route. Sending response: {}", response);

                        try {
                            channel.basicPublish("", to, reply, response.getBytes(StandardCharsets.UTF_8));
                            logger.info("Response sent to reply-to queue '{}'", to);
                        } catch (IOException e) {
                            logger.error("Failed to send response: {}", e.getMessage(), e);
                            throw new RuntimeException(e);
                        }
                    });
                } catch (Exception e) {
                    logger.error("Error processing message: {}", e.getMessage(), e);
                }
            }, consumer -> {});
        } catch (Exception e) {
            logger.error("Failed to initialize Application: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void close() throws IOException {
       if (this.connection != null) {
           this.connection.close();
           logger.info("RabbitMQ connection closed successfully.");
       }
    }
}
