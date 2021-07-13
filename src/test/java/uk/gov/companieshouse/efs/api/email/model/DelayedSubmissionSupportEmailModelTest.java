package uk.gov.companieshouse.efs.api.email.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;

import java.util.Collections;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DelayedSubmissionSupportEmailModelTest {
    private DelayedSubmissionSupportEmailModel testModel;

    @BeforeEach
    void setUp() {
        testModel = new DelayedSubmissionSupportEmailModel(null, 0);
    }

    @Test
    void setGetDelayedSubmissions() {
        testModel.setDelayedSubmissions(Collections.emptyList());
        assertThat(testModel.getDelayedSubmissions(), is(empty()));
    }

    @Test
    void getNumberOfDelayedSubmissions() {
        testModel.setDelayedSubmissions(
            Collections.singletonList(new DelayedSubmissionSupportModel(null, null, null,
                null, null)));
        assertThat(testModel.getNumberOfDelayedSubmissions(), is(1));
    }

    @Test
    void setGetThresholdInMinutes() {
        testModel.setThresholdInMinutes(3000);
        assertThat(testModel.getThresholdInMinutes(), is(3000));
    }

    @Test
    void testEquals() {
        EqualsVerifier.forClass(DelayedSubmissionSupportEmailModel.class).usingGetClass().suppress(
            Warning.NONFINAL_FIELDS).verify();
    }

    @Test
    void testToString() {
        assertThat(testModel.toString(),
            is("DelayedSubmissionSupportEmailModel[delayedSubmissions=<null>,thresholdInMinutes=0]"));
    }
}