package uk.gov.companieshouse.efs.api.util;

import org.testcontainers.containers.OracleContainer;

public class OracleTestContainer {
    public static final OracleTestContainer INSTANCE = new OracleTestContainer();
    private static final String IMAGE = "gvenzl/oracle-xe:21-slim";
    private final OracleContainer container;

    private OracleTestContainer() {
        container = new OracleContainer(IMAGE)
                .withDatabaseName("testdb")
                .withUsername("testuser")
                .withPassword("testpass");
        container.start();
    }

    public String getJdbcUrl() {
        return container.getJdbcUrl();
    }

    public String getUsername() {
        return container.getUsername();
    }

    public String getPassword() {
        return container.getPassword();
    }
}

