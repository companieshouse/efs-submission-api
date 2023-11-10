package uk.gov.companieshouse.efs.api.health;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.logging.Logger;

@Component
@ConditionalOnEnabledHealthIndicator("maintenance")
public class MaintenanceHealthIndicator implements HealthIndicator {

    private static final String NO_PLANNED_MAINTENANCE_CONFIGURED =
        "No planned maintenance is " + "configured";
    private final Clock clock;
    private final Logger logger;

    @Value("${out-of-service.period.start}")
    private String outOfServiceStart;

    @Value("${out-of-service.period.end}")
    private String outOfServiceEnd;

    @Value("${out-of-service.period.message}")
    private String outOfServiceMessage;

    @Autowired
    public MaintenanceHealthIndicator(final Clock clock, final Logger logger) {
        this.clock = clock;
        this.logger = logger;
    }

    /**
     * @return
     */
    @Override
    public Health health() {
        final ZonedDateTime startDateTime;
        final ZonedDateTime endDateTime;

        try {
            startDateTime = StringUtils.isBlank(outOfServiceStart) ? null : parseZonedDateTime(
                outOfServiceStart);
        } catch (DateTimeParseException e) {
            logger.error(
                "Error parsing configuration: PLANNED_MAINTENANCE_START_TIME: " + e.getMessage());

            return buildBadConfigurationResponse(e,
                "Error parsing configuration: PLANNED_MAINTENANCE_START_TIME");
        }
        try {
            endDateTime = StringUtils.isBlank(outOfServiceEnd) ? null : parseZonedDateTime(
                outOfServiceEnd);
        } catch (DateTimeParseException e) {
            logger.error(
                "Error parsing configuration: PLANNED_MAINTENANCE_END_TIME: " + e.getMessage());

            return buildBadConfigurationResponse(e,
                "Error parsing configuration: PLANNED_MAINTENANCE_END_TIME");
        }

        final ZonedDateTime now = ZonedDateTime.now(clock);

        if (startDateTime != null && endDateTime != null) {
            // non-inclusive range: startDateTime < now < endDateTime
            if (now.isAfter(startDateTime) && now.isBefore(endDateTime)) {
                logger.info("Planned maintenance is ongoing - ending at " + endDateTime.format(
                    DateTimeFormatter.ISO_INSTANT));

                return Health.outOfService()
                    .withDetail("message", outOfServiceMessage)
                    .withDetail("maintenance_start_time",
                        startDateTime.format(DateTimeFormatter.ISO_INSTANT))
                    .withDetail("maintenance_end_time",
                        endDateTime.format(DateTimeFormatter.ISO_INSTANT))
                    .build();
            }
            else {
                logger.info("No planned maintenance is ongoing");
                return Health.up()
                    .withDetail("message", "Planned maintenance has been configured")
                    .withDetail("maintenance_start_time",
                        startDateTime.format(DateTimeFormatter.ISO_INSTANT))
                    .withDetail("maintenance_end_time",
                        endDateTime.format(DateTimeFormatter.ISO_INSTANT))
                    .build();
            }
        }
        else {
            logger.info(NO_PLANNED_MAINTENANCE_CONFIGURED);
            return Health.up()
                .withDetail("message", "No planned maintenance is configured")
                .build();
        }
    }

    private Health buildBadConfigurationResponse(DateTimeParseException e, final String message) {
        return Health.up()
            .withDetail("message", message)
            .withDetail("value", outOfServiceStart)
            .withDetail("error", e.getMessage())
            .build();
    }

    private static ZonedDateTime parseZonedDateTime(final String zoned) {
        return ZonedDateTime.parse(zoned,
            DateTimeFormatter.ofPattern("d MMM yy HH:mm[ z][ x]", Locale.ENGLISH));
    }
}
