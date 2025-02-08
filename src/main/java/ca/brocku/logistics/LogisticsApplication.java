package ca.brocku.logistics;

import ca.brocku.logistics.model.GeoJsonFeature;
import ca.brocku.logistics.model.Route;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.rabbitmq.client.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.GeneticAlgorithm2;
import org.example.Location;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class LogisticsApplication implements Closeable {
    private static final Logger logger = LogManager.getLogger(LogisticsApplication.class);
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    private static final String QUEUE = "logistic-request";

    private final Connection connection;
    private final Channel channel;

    public LogisticsApplication(String host) throws IOException, TimeoutException, URISyntaxException, NoSuchAlgorithmException, KeyManagementException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri(host);
        this.connection = factory.newConnection();
        this.channel = connection.createChannel();

        channel.queueDeclare(QUEUE, true, false, false, null);
        channel.basicQos(1);
        logger.info("Connected to RabbitMQ at host: {}", host);

        setupConsumer();
    }

    private void setupConsumer() throws IOException {
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            try {
                processMessage(delivery);
            } catch (Exception e) {
                logger.error("Error processing message: {}", e.getMessage(), e);
                channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
            }
        };

        channel.basicConsume(QUEUE, false, deliverCallback, consumerTag ->
                logger.info("Consumer {} cancelled", consumerTag));
    }

    private void processMessage(Delivery delivery) throws IOException {
        String content = new String(delivery.getBody(), StandardCharsets.UTF_8);
        AMQP.BasicProperties properties = delivery.getProperties();

        logger.info("Processing message: {}", content);

        Type listType = new TypeToken<List<GeoJsonFeature>>(){}.getType();
        List<GeoJsonFeature> features = GSON.fromJson(content, listType);

        List<Location> locations = convertFeaturesToLocations(features);
        GeneticAlgorithm2 ga = new GeneticAlgorithm2(1000, 0.75, 0.2, 3,
                features.size() * features.size(), 42, locations);

        List<GeoJsonFeature> optimizedRoute = convertLocationsToGeoJson(ga.mainLoop().getRoute());
        String response = GSON.toJson(optimizedRoute);

        AMQP.BasicProperties replyProps = new AMQP.BasicProperties.Builder()
                .correlationId(properties.getCorrelationId())
                .build();

        channel.basicPublish("", properties.getReplyTo(), replyProps,
                response.getBytes(StandardCharsets.UTF_8));
        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

        logger.info("Route optimization completed and response sent");
    }

    private List<Location> convertFeaturesToLocations(List<GeoJsonFeature> features) {
        List<Location> locations = new ArrayList<>();
        for (int i = 0; i < features.size(); i++) {
            GeoJsonFeature feature = features.get(i);
            List<Double> coords = feature.getGeometry().getCoordinates();
            locations.add(new Location(coords.get(1), coords.get(0), i));
        }
        return locations;
    }

    private List<GeoJsonFeature> convertLocationsToGeoJson(List<Location> locations) {
        List<GeoJsonFeature> features = new ArrayList<>();
        for (Location location : locations) {
            GeoJsonFeature feature = new GeoJsonFeature();
            feature.setType("Feature");

            GeoJsonFeature.Geometry geometry = new GeoJsonFeature.Geometry();
            geometry.setType("Point");
            geometry.setCoordinates(Arrays.asList(location.getLon(), location.getLat()));
            feature.setGeometry(geometry);

            GeoJsonFeature.Properties properties = new GeoJsonFeature.Properties();
            properties.setOrder(location.getID());
            properties.setAddress("Location " + location.getID());
            feature.setProperties(properties);

            features.add(feature);
        }
        return features;
    }

    @Override
    public void close() throws IOException {
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
            if (connection != null && connection.isOpen()) {
                connection.close();
            }
            logger.info("RabbitMQ connections closed successfully");
        } catch (TimeoutException e) {
            throw new IOException("Failed to close connections", e);
        }
    }
}