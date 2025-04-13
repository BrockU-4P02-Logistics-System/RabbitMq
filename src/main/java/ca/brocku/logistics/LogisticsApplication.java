package ca.brocku.logistics;

import ca.brocku.logistics.model.GeoJsonFeature;
import ca.brocku.logistics.model.MessageRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.rabbitmq.client.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.*;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.TimeoutException;

public class LogisticsApplication implements Closeable {
    private static final Logger logger = LogManager.getLogger(LogisticsApplication.class);
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    private static final String QUEUE = "logistic-request";

    private final Connection connection;
    private final Channel channel;
    private final EntryPoint entryPoint = new EntryPoint();

    public LogisticsApplication(String host) throws IOException, TimeoutException, URISyntaxException, NoSuchAlgorithmException, KeyManagementException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri(host);
        this.connection = factory.newConnection();
        this.channel = connection.createChannel();

        channel.queueDeclare(QUEUE, true, false, false, null);
        channel.basicQos(1);
        System.out.println("Connected to RabbitMQ at host: " + host);

        setupConsumer();
    }

    private void setupConsumer() throws IOException {
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            try {
                processMessage(delivery);
            } catch (Exception e) {
                logger.error("Error processing message: {}", e.getMessage(), e);
                // If the error is non-transient, acknowledge the message to avoid requeueing it
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }
        };
        channel.basicConsume(QUEUE, false, deliverCallback, consumerTag ->
                System.out.println("Consumer " + consumerTag + " cancelled"));
    }



    private void processMessage(Delivery delivery) throws IOException {
    String content = new String(delivery.getBody(), StandardCharsets.UTF_8);
    AMQP.BasicProperties properties = delivery.getProperties();

    System.out.println("Processing message: " + content);

    // Parse the message as a JSON object
    Type messageType = new TypeToken<MessageRequest>(){}.getType();
    MessageRequest request = GSON.fromJson(content, messageType);

    List<GeoJsonFeature> features = request.getFeatures();
    int numberDrivers = request.getNumberDrivers();
    boolean returnToStart = request.isReturnToStart();

    System.out.println("Number of drivers: " + numberDrivers + " Return to start: " + returnToStart);

    // Convert features to locations
    List<Location> locations = convertFeaturesToLocations(features);

    // Configure the router
    boolean[] options = {false, false, false};

    // Solve the routing problem
    Route route = entryPoint.spawnWorker(locations, options, numberDrivers, returnToStart);

    // Create a list to store all routes for all drivers
    List<List<GeoJsonFeature>> allDriverRoutes = new ArrayList<>();

    // Process each driver's route
    for (int i = 0; i < route.getFinalMultiRoute().size(); i++) {
        final List<Location> driverStops = route.getFinalMultiRoute().get(i);
        // Convert this driver's route to GeoJSON
        List<GeoJsonFeature> driverRoute = convertLocationsToGeoJson(driverStops, i);
        allDriverRoutes.add(driverRoute);
    }

    // Create the final response with all driver routes
    Map<String, Object> responseMap = new HashMap<>();
    responseMap.put("route", allDriverRoutes);
    String response = GSON.toJson(responseMap);

    // Send the response
    AMQP.BasicProperties replyProps = new AMQP.BasicProperties.Builder()
            .correlationId(properties.getCorrelationId())
            .build();

    channel.basicPublish("", properties.getReplyTo(), replyProps,
            response.getBytes(StandardCharsets.UTF_8));
    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

    logger.info("Route optimization completed and response sent for " + numberDrivers + " drivers");
}

// Updated to include driver ID in the properties
private List<GeoJsonFeature> convertLocationsToGeoJson(List<Location> locations, int driverId) {
    List<GeoJsonFeature> features = new ArrayList<>();
    for (int i = 0; i < locations.size(); i++) {
        Location location = locations.get(i);
        GeoJsonFeature feature = new GeoJsonFeature();
        feature.setType("Feature");

        GeoJsonFeature.Geometry geometry = new GeoJsonFeature.Geometry();
        geometry.setType("Point");
        geometry.setCoordinates(Arrays.asList(location.getLon(), location.getLat()));
        feature.setGeometry(geometry);

        GeoJsonFeature.Properties properties = new GeoJsonFeature.Properties();
        properties.setOrder(i+1); // Sequential order within this driver's route
        properties.setAddress("Location " + location.getID());
        properties.setDriverId(driverId); // Add driver ID to properties
        feature.setProperties(properties);

        features.add(feature);
    }
    return features;
}

    private List<Location> convertFeaturesToLocations(List<GeoJsonFeature> features) {
        List<Location> locations = new ArrayList<>();
        for (int i = 0; i < features.size(); i++) {
            GeoJsonFeature feature = features.get(i);
            List<Double> coords = feature.getGeometry().getCoordinates();
            locations.add(new Location(coords.get(1), coords.get(0), i+1));
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