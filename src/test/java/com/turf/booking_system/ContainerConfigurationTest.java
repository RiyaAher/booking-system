package com.turf.booking_system;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class ContainerConfigurationTest {

    @Test
    void containerDefaultsShouldUseEmbeddedH2Database() throws IOException {
        String applicationProperties = Files.readString(
                Path.of("src/main/resources/application.properties"),
                StandardCharsets.UTF_8
        );

        assertTrue(applicationProperties.contains("jdbc:h2:mem:bookingdb"));
        assertTrue(applicationProperties.contains("org.h2.Driver"));
    }

    @Test
    void h2DriverShouldBeAvailableAtRuntime() throws IOException {
        String pomXml = Files.readString(
                Path.of("pom.xml"),
                StandardCharsets.UTF_8
        );

        assertTrue(pomXml.contains("<artifactId>h2</artifactId>"));
        assertTrue(pomXml.contains("<scope>runtime</scope>"));
    }
}
