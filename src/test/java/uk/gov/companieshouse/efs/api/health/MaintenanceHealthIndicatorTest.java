package uk.gov.companieshouse.efs.api.health;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class MaintenanceHealthIndicatorTest {
    private static final Instant FIXED_NOW = Instant.parse("2023-12-25T01:23:45Z");

    private MaintenanceHealthIndicator testIndicator;

    @Mock
    private Logger logger;

    @BeforeEach
    void setUp() {
        testIndicator = new MaintenanceHealthIndicator(Clock.fixed(FIXED_NOW, ZoneId.of("UTC")),
            logger);
    }

    @Test
    void healthWhenStartAndEndTimesBlankOrNull() {
        setupPeriodConfigValues("", null, null);

        final Health healthNow = testIndicator.health();

        assertThat(healthNow.getStatus(), is(Status.UP));
        assertThat(healthNow.getDetails(),
            hasEntry("message", "No planned maintenance is configured"));

    }

    @Test
    void healthWhenOnlyStartTimeNotBlank() {
        setupPeriodConfigValues("3 Dec 23 00:30 GMT", null, null);

        final Health healthNow = testIndicator.health();

        assertThat(healthNow.getStatus(), is(Status.UP));
        assertThat(healthNow.getDetails(),
            hasEntry("message", "No planned maintenance is configured"));

    }

    @Test
    void healthWhenOnlyEndTimeNotBlank() {
        setupPeriodConfigValues("", "3 Dec 23 00:30 GMT", null);

        final Health healthNow = testIndicator.health();

        assertThat(healthNow.getStatus(), is(Status.UP));
        assertThat(healthNow.getDetails(),
            hasEntry("message", "No planned maintenance is configured"));

    }

    @Test
    void healthWhenPeriodSetForFuture() {
        setupPeriodConfigValues("3 Jan 24 00:30 GMT", "3 Jan 24 02:30 GMT", null);

        final Health healthNow = testIndicator.health();

        assertThat(healthNow.getStatus(), is(Status.UP));
        assertThat(healthNow.getDetails(),
            hasEntry("message", "Planned maintenance has been configured"));
        assertThat(healthNow.getDetails(),
            hasEntry("maintenance_start_time", "2024-01-03T00:30:00Z"));
        assertThat(healthNow.getDetails(),
            hasEntry("maintenance_end_time", "2024-01-03T02:30:00Z"));

    }

    @Test
    void healthWhenPeriodSetForPast() {
        setupPeriodConfigValues("3 Dec 23 00:30 GMT", "3 Dec 23 02:30 GMT", null);

        final Health healthNow = testIndicator.health();

        assertThat(healthNow.getStatus(), is(Status.UP));
        assertThat(healthNow.getDetails(),
            hasEntry("message", "Planned maintenance has been configured"));
        assertThat(healthNow.getDetails(),
            hasEntry("maintenance_start_time", "2023-12-03T00:30:00Z"));
        assertThat(healthNow.getDetails(),
            hasEntry("maintenance_end_time", "2023-12-03T02:30:00Z"));

    }

    @Test
    void healthWhenPeriodSetAndOngoing() {
        setupPeriodConfigValues("25 Dec 23 00:30 GMT", "25 Dec 23 02:30 GMT",
            "UNAVAILABLE - PLANNED MAINTENANCE");

        final Health healthNow = testIndicator.health();

        assertThat(healthNow.getStatus(), is(Status.OUT_OF_SERVICE));
        assertThat(healthNow.getDetails(),
            hasEntry("message", "UNAVAILABLE - PLANNED MAINTENANCE"));
        assertThat(healthNow.getDetails(),
            hasEntry("maintenance_start_time", "2023-12-25T00:30:00Z"));
        assertThat(healthNow.getDetails(),
            hasEntry("maintenance_end_time", "2023-12-25T02:30:00Z"));

    }

    @Test
    void healthWhenStartTimeInvalidNoTimezone() {
        setupPeriodConfigValues("25 Dec 23 00:30", "25 Dec 23 02:30 GMT",
            "UNAVAILABLE - PLANNED MAINTENANCE");

        final Health healthNow = testIndicator.health();

        assertThat(healthNow.getStatus(), is(Status.UP));
        assertThat(healthNow.getDetails(),
            hasEntry("message", "Error parsing configuration: PLANNED_MAINTENANCE_START_TIME"));
        assertThat(healthNow.getDetails(), hasEntry("value", "25 Dec 23 00:30"));
        assertThat(healthNow.getDetails(),
            hasEntry("error",
                "Text '25 Dec 23 00:30' could not be parsed: Unable to obtain ZonedDateTime from "
                    + "TemporalAccessor: {},ISO resolved to 2023-12-25T00:30 of type java.time"
                    + ".format.Parsed"));

    }

    @Test
    void healthWhenEndTimeInvalidNoSpaceAfterTime() {
        setupPeriodConfigValues("25 Dec 23 00:30 GMT", "5 Jan 24 02:30+01",
            "UNAVAILABLE - PLANNED MAINTENANCE");

        final Health healthNow = testIndicator.health();

        assertThat(healthNow.getStatus(), is(Status.UP));
        assertThat(healthNow.getDetails(),
            hasEntry("message", "Error parsing configuration: PLANNED_MAINTENANCE_END_TIME"));
        assertThat(healthNow.getDetails(),
            hasEntry("error",
                "Text '5 Jan 24 02:30+01' could not be parsed, unparsed text found at index 14"));

    }

    private void setupPeriodConfigValues(final String start, final String end,
        final String message) {
        ReflectionTestUtils.setField(testIndicator, "outOfServiceStart", start);
        ReflectionTestUtils.setField(testIndicator, "outOfServiceEnd", end);
        ReflectionTestUtils.setField(testIndicator, "outOfServiceMessage", message);
    }


}