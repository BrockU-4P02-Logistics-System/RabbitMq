# RabbitMQ Consumer

A lightweight Java client demonstrating how to connect and consume messages from RabbitMQ.

## Overview

This repository contains sample code for a consumer that listens on a queue and processes incoming messages.

## Prerequisites

- Java Development Kit (JDK) 19 or higher
- Gradle (wrapper included)
- A running RabbitMQ server (default `amqp://localhost:5672`)
- Allocate at least **30 GB** of heap memory for the JVM (e.g., `java -Xmx30g`)

## Installation & Build

Clone the repository and build a fat JAR using the Gradle Shadow plugin:

```bash
git clone https://github.com/your-org/rabbitmq-samples.git
cd rabbitmq-samples
./gradlew shadowJar
```

After the build completes, the runnable JAR will be located in `build/libs/`.

## Configuration

Before building, update the source code to use your own connection details:

- RabbitMQ broker URL
- Queue name
- Exchange name (if using an exchange)
- Routing key (if using an exchange)

For example, set values like:
```java
// replace these with your own values
String rabbitmqUrl = "amqp://<USER>:<PASS>@<HOST>:5672";
String queueName    = "<YOUR_QUEUE_NAME>";
String exchangeName = "<YOUR_EXCHANGE_NAME>";
String routingKey   = "<YOUR_ROUTING_KEY>";
```

Rebuild the JAR after making changes.

## Usage

Run the consumer with the recommended heap size:

```bash
java -Xmx30g -jar build/libs/rabbitmq-samples-all.jar consumer
```

The application will:

1. Connect to RabbitMQ.
2. Declare or verify the queue exists.
3. Bind to the specified exchange and routing key (if used).
4. Begin consuming messages and handling them according to the sample implementation.

## Project Structure

```
├── src/main/java/.../           # Java source files for consumer logic
├── build.gradle.kts            # Gradle build with Shadow plugin
├── settings.gradle.kts         # Project settings
└── LICENSE                     # Apache 2.0 license file
```

## Troubleshooting

- Ensure the broker URL and credentials in the code match your RabbitMQ setup.
- Confirm queue and exchange names are consistent with your RabbitMQ configuration.
- Check firewall or network settings if connecting to a remote broker.

## License

Apache 2.0 License. See the LICENSE file for details.

