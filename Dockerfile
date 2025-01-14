FROM openjdk:19-jdk-slim

WORKDIR /app

COPY build/libs/ca.brocku.logistics-1.0-SNAPSHOT.jar app.jar

CMD ["java", "-jar", "logisitics-rabbit-1.0-SNAPSHOT.jar"]