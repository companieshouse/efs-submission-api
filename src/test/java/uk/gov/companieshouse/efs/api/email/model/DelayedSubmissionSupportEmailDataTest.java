package uk.gov.companieshouse.efs.api.email.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;

import java.util.Collections;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DelayedSubmissionSupportEmailDataTest {
    private DelayedSubmissionSupportEmailData testData;

    @BeforeEach
    void setUp() {
        testData = new DelayedSubmissionSupportEmailData(null, null, null, 0);
    }

    @Test
    void setGetTo() {
        testData.setTo("expected");
        assertThat(testData.getTo(), is("expected"));
    }

    @Test
    void setGetSubject() {
        testData.setSubject("expected");
        assertThat(testData.getSubject(), is("expected"));
    }

    @Test
    void setGetDelayedSubmissions() {
        final DelayedSubmissionSupportModel model =
            new DelayedSubmissionSupportModel(null, null, null, null, null);

        testData.setDelayedSubmissions(Collections.singletonList(model));
        assertThat(testData.getDelayedSubmissions(), contains(model));
    }

    @Test
    void setGetThresholdInMinutes() {
        testData.setThresholdInMinutes(3000);
        assertThat(testData.getThresholdInMinutes(), is(3000));
    }

    @Test
    void testEquals() {
        EqualsVerifier.forClass(DelayedSubmissionSupportEmailData.class)
            .usingGetClass()
            .suppress(Warning.NONFINAL_FIELDS)
            .verify();
    }

    @Test
    void testToString() {
        assertThat(testData.toString(),
            is("DelayedSubmissionSupportEmailData[delayedSubmissions=<null>,subject=<null>," 
                + "thresholdInMinutes=0,to=<null>]"));
    }
}