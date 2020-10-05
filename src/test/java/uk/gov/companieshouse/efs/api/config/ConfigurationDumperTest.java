package uk.gov.companieshouse.efs.api.config;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
class ConfigurationDumperTest {
    @Mock
    ApplicationContextEvent event;
    @Mock
    private ApplicationContext context;
    @Mock
    private Logger logger;
    @Captor
    private ArgumentCaptor<Map<String, Object> > propertyMap;

    MockEnvironment env;

    private ConfigurationDumper testDumper;

    @BeforeEach
    void setUp() {
        testDumper = new ConfigurationDumper(logger);
        env = new MockEnvironment().withProperty("VAR_ONE", "1")
            .withProperty("my_credentials", "secret")
            .withProperty("admin_password", "super-secret");
        env.setActiveProfiles("test");
    }

    @Test
    void handleContextRefresh() {
        // given
        when(event.getApplicationContext()).thenReturn(context);
        when(context.getEnvironment()).thenReturn(env);

        // when
        testDumper.handleContextRefresh(event);

        // then
        verify(event).getApplicationContext();
        verify(context).getEnvironment();

        InOrder logOrder = inOrder(logger);

        logOrder.verify(logger).trace("Active profiles: " + "[test]");
        logOrder.verify(logger).trace(eq("PROPERTIES"), propertyMap.capture());
        logOrder.verifyNoMoreInteractions();
        MatcherAssert.assertThat(propertyMap.getValue().keySet(), hasSize(1));
        MatcherAssert.assertThat(propertyMap.getValue(), hasEntry("VAR_ONE", "1"));
    }

}