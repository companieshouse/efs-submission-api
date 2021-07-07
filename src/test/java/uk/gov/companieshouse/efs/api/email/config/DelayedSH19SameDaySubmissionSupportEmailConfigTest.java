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