package uk.gov.companieshouse.efs.api.email.config;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DelayedSubmissionSupportEmailConfigTest {
    private DelayedSubmissionSupportEmailConfig testConfig;

    @BeforeEach
    void setUp() {
        testConfig = new DelayedSubmissionSupportEmailConfig();
    }

    @Test
    void getSupportEmailAddress() {
        assertThat(testConfig.getSupportEmailAddress(), is(nullValue()));
    }

    @Test
    void setSupportEmailAddress() {
        testConfig.setSupportEmailAddress("expected");
        assertThat(testConfig.getSupportEmailAddress(), is("expected"));
    }

    @Test
    void testEqualsAndHashcode() {
        EqualsVerifier.forClass(DelayedSubmissionSupportEmailConfig.class).usingGetClass().suppress(
            Warning.NONFINAL_FIELDS).verify();
    }
    
    
}