package uk.gov.companieshouse.efs.api.payment.controller;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;

@Configuration
@EnableMongoRepositories
public class MongoDbTestContainerConfig {
    @Container
    public static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7")
        .withExposedPorts(27017);

    static {
        mongoDBContainer.start();

        Integer mappedPort = mongoDBContainer.getMappedPort(27017);

        System.setProperty("mongodb.container.port", String.valueOf(mappedPort));
    }
}
