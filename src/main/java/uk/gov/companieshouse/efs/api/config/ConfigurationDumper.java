package uk.gov.companieshouse.efs.api.config;

import java.util.Arrays;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import uk.gov.companieshouse.logging.Logger;

/**
 * Outputs logs which contains the current environment and configurations.
 */
@Configuration
public class ConfigurationDumper {
    private final Logger logger;

    /**
     * Constructor to set the logger used to output the information.
     *
     * @param logger the configured logger
     */
    @Autowired
    public ConfigurationDumper(final Logger logger) {
        this.logger = logger;
    }

    /**
     * Collates all the environment configuration and Spring properties and logs out for debug purposes.
     * Exclude any properties with names containing "password" or "credentials".
     *
     * @param event the {@link ApplicationContextEvent} for events raised for an ApplicationContext
     */
    @EventListener
    public void handleContextRefresh(ApplicationContextEvent event) {
        final Environment env = event.getApplicationContext().getEnvironment();
        final String activeProfiles = Arrays.toString(env.getActiveProfiles());

        SortedMap<String, Object> map = new TreeMap<>();
        for (final PropertySource<?> source : ((AbstractEnvironment) env).getPropertySources()) {
            if ((PropertySource) source instanceof MapPropertySource) {
                final Map<String, Object> sourceMap = ((MapPropertySource) source).getSource();

                map.putAll(sourceMap.entrySet().stream()
                    .filter(prop -> !(prop.getKey().contains("credentials") || prop.getKey().contains("password")))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
            }
        }
        logger.trace("Active profiles: " + activeProfiles);
        logger.trace("PROPERTIES", map);
    }
}
