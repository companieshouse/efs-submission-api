package uk.gov.companieshouse.efs.api.email.config;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DelayedSH19SameDaySubmissionSupportEmailConfigTest {
    private DelayedSH19SameDaySubmissionSupportEmailConfig testConfig;

    @BeforeEach
    void setUp() {
        testConfig = new DelayedSH19SameDaySubmissionSupportEmailConfig();
    }

    @Test
    void setGetAppId() {
        testConfig.setAppId("expected");
        assertThat(testConfig.getAppId(), is("expected"));
    }

    @Test
    void setGetSubject() {
        testConfig.setSubject("expected");
        assertThat(testConfig.getSubject(), is("expected"));
    }

    @Test
    void setGetTopic() {
        testConfig.setTopic("expected");
        assertThat(testConfig.getTopic(), is("expected"));
    }

    @Test
    void setGetMessageType() {
        testConfig.setMessageType("expected");
        assertThat(testConfig.getMessageType(), is("expected"));
    }

    @Test
    void setDateFormat() {
        testConfig.setDateFormat("expected");
        assertThat(testConfig.getDateFormat(), is("expected"));
    }

    @Test
    void setGetSupportEmailAddress() {
        testConfig.setSupportEmailAddress("email");
        assertThat(testConfig.getSupportEmailAddress(), is("email"));
    }

    @Test
    void testEqualsAndHashcode() {
        EqualsVerifier.forClass(DelayedSH19SameDaySubmissionSupportEmailConfig.class)
            .usingGetClass()
            .suppress(Warning.NONFINAL_FIELDS)
            .verify();
    }

}