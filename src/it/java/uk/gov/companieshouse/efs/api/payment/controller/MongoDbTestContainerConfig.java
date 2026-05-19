package uk.gov.companieshouse.efs.api.payment.controller;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.testcontainers.mongodb.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.junit.jupiter.Container;

@Configuration
@EnableMongoRepositories
public class MongoDbTestContainerConfig {
    @Container
    public static MongoDBContainer mongoDBContainer = new MongoDBContainer(
        DockerImageName.parse("mongo:8")
    ).withExposedPorts(27017);

    static {
        mongoDBContainer.start();

        final var mappedPort = mongoDBContainer.getMappedPort(27017);

        System.setProperty("mongodb.container.port", String.valueOf(mappedPort));
    }
}
