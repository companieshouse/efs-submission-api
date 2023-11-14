package uk.gov.companieshouse.efs.api.maintenance;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.companieshouse.api.model.efs.maintenance.MaintenanceCheckApi;
import uk.gov.companieshouse.api.model.efs.maintenance.ServiceStatus;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class MaintenanceActuatorEndpointTest {
    private static final Instant FIXED_NOW = Instant.parse("2023-12-25T01:23:45Z");

    private MaintenanceActuatorEndpoint testEndpoint;

    @Mock
    private Logger logger;

    @BeforeEach
    void setUp() {
        testEndpoint = new MaintenanceActuatorEndpoint(Clock.fixed(FIXED_NOW, ZoneId.of("UTC")),
            logger);
    }

    public static Stream<Arguments> blankConfigs() {
        return Stream.of(arguments("", null, null), arguments("3 Dec 23 00:30 GMT", null, null),
            arguments("", "3 Dec 23 00:30 GMT", null));
    }

    @ParameterizedTest
    @MethodSource("blankConfigs")
    void resultWhenTimesAreBlank(final String start, final String end, final String message) {
        setupPeriodConfigValues(start, end, message);

        final ResponseEntity<MaintenanceCheckApi> resultNow = testEndpoint.check();

        assertThat(resultNow.getStatusCode(), is(HttpStatus.OK));
        assertThat(resultNow.getBody().getStatus(), is(ServiceStatus.UP));
        assertThat(resultNow.getBody().getMessage(), is("No planned maintenance is configured"));

    }

    @Test
    void checkWhenPeriodSetForFuture() {
        setupPeriodConfigValues("3 Jan 24 00:30 GMT", "3 Jan 24 02:30 GMT", null);

        final ResponseEntity<MaintenanceCheckApi> resultNow = testEndpoint.check();

        assertThat(resultNow.getStatusCode(), is(HttpStatus.OK));
        assertThat(resultNow.getBody().getStatus(), is(ServiceStatus.UP));
        assertThat(resultNow.getBody().getMessage(), is("Planned maintenance has been configured"));
        assertThat(resultNow.getBody().getMaintenanceStart(), is("2024-01-03T00:30:00Z"));
        assertThat(resultNow.getBody().getMaintenanceEnd(), is("2024-01-03T02:30:00Z"));

    }

    @Test
    void checkWhenPeriodSetForPast() {
        setupPeriodConfigValues("3 Dec 23 00:30 GMT", "3 Dec 23 02:30 GMT", null);

        final ResponseEntity<MaintenanceCheckApi> resultNow = testEndpoint.check();

        assertThat(resultNow.getStatusCode(), is(HttpStatus.OK));
        assertThat(resultNow.getBody().getStatus(), is(ServiceStatus.UP));
        assertThat(resultNow.getBody().getMessage(), is("Planned maintenance has been configured"));
        assertThat(resultNow.getBody().getMaintenanceStart(), is("2023-12-03T00:30:00Z"));
        assertThat(resultNow.getBody().getMaintenanceEnd(), is("2023-12-03T02:30:00Z"));

    }

    @Test
    void checkWhenPeriodSetAndOngoing() {
        setupPeriodConfigValues("25 Dec 23 00:30 GMT", "25 Dec 23 02:30 GMT",
            "UNAVAILABLE - PLANNED MAINTENANCE");

        final ResponseEntity<MaintenanceCheckApi> resultNow = testEndpoint.check();

        assertThat(resultNow.getStatusCode(), is(HttpStatus.SERVICE_UNAVAILABLE));
        assertThat(resultNow.getBody().getStatus(), is(ServiceStatus.OUT_OF_SERVICE));
        assertThat(resultNow.getBody().getMessage(), is("UNAVAILABLE - PLANNED MAINTENANCE"));
        assertThat(resultNow.getBody().getMaintenanceStart(), is("2023-12-25T00:30:00Z"));
        assertThat(resultNow.getBody().getMaintenanceEnd(), is("2023-12-25T02:30:00Z"));

    }

    @Test
    void checkWhenStartTimeInvalidNoTimezone() {
        setupPeriodConfigValues("25 Dec 23 00:30", "25 Dec 23 02:30 GMT",
            "UNAVAILABLE - PLANNED MAINTENANCE");

        final ResponseEntity<MaintenanceCheckApi> resultNow = testEndpoint.check();

        assertThat(resultNow.getStatusCode(), is(HttpStatus.OK));
        assertThat(resultNow.getBody().getStatus(), is(ServiceStatus.UP));
        assertThat(resultNow.getBody().getMessage(),
            is("Error parsing configuration: PLANNED_MAINTENANCE_START_TIME: Text '25 Dec 23 " +
                "00:30' could not be parsed: Unable to obtain ZonedDateTime from " +
                "TemporalAccessor: {},ISO resolved to 2023-12-25T00:30 of type java.time.format"
                + ".Parsed"));
        assertThat(resultNow.getBody().getMaintenanceStart(), is("25 Dec 23 00:30"));
        assertThat(resultNow.getBody().getMaintenanceEnd(), is("25 Dec 23 02:30 GMT"));
    }

    @Test
    void checkWhenEndTimeInvalidNoSpaceAfterTime() {
        setupPeriodConfigValues("25 Dec 23 00:30 GMT", "5 Jan 24 02:30+01",
            "UNAVAILABLE - PLANNED MAINTENANCE");

        final ResponseEntity<MaintenanceCheckApi> resultNow = testEndpoint.check();

        assertThat(resultNow.getStatusCode(), is(HttpStatus.OK));
        assertThat(resultNow.getBody().getStatus(), is(ServiceStatus.UP));
        assertThat(resultNow.getBody().getMessage(),
            is("Error parsing configuration: PLANNED_MAINTENANCE_END_TIME: Text '5 Jan 24 " + "02"
                + ":30+01' could not be parsed, unparsed text found at index 14"));
        assertThat(resultNow.getBody().getMaintenanceStart(), is("2023-12-25T00:30:00Z"));
        assertThat(resultNow.getBody().getMaintenanceEnd(), is("5 Jan 24 02:30+01"));
    }

    @Test
    void checkWhenEndTimeInvalidSingleDigitOffset() {
        setupPeriodConfigValues("", "5 Jan 24 02:30+01",
            "UNAVAILABLE - PLANNED MAINTENANCE");

        final ResponseEntity<MaintenanceCheckApi> resultNow = testEndpoint.check();

        assertThat(resultNow.getStatusCode(), is(HttpStatus.OK));
        assertThat(resultNow.getBody().getStatus(), is(ServiceStatus.UP));
        assertThat(resultNow.getBody().getMessage(),
            is("Error parsing configuration: PLANNED_MAINTENANCE_END_TIME: Text '5 Jan 24 " + "02"
                + ":30+01' could not be parsed, unparsed text found at index 14"));
        assertThat(resultNow.getBody().getMaintenanceStart(), is(nullValue()));
        assertThat(resultNow.getBody().getMaintenanceEnd(), is("5 Jan 24 02:30+01"));
    }

    private void setupPeriodConfigValues(final String start, final String end,
        final String message) {
        ReflectionTestUtils.setField(testEndpoint, "outOfServiceStart", start);
        ReflectionTestUtils.setField(testEndpoint, "outOfServiceEnd", end);
        ReflectionTestUtils.setField(testEndpoint, "outOfServiceMessage", message);
    }

}