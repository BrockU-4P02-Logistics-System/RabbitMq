plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "ca.brocku.logistics"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("com.rabbitmq:amqp-client:5.24.0")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("org.apache.logging.log4j:log4j-core:2.24.3")
    implementation(fileTree("libs"))
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    archiveClassifier.set("")
    manifest {
        attributes(
            "Main-Class" to "ca.brocku.logistics.Main"
        )
    }
}

tasks {
    build {
        dependsOn("shadowJar")
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(19))
    }
}