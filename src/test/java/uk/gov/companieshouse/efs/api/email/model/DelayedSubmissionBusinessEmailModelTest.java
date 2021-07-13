package uk.gov.companieshouse.efs.api.email.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.nullValue;

import java.util.Collections;
import java.util.List;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DelayedSubmissionBusinessEmailModelTest {
    private static final int DELAY_IN_MINUTES = 60;
    private DelayedSubmissionBusinessEmailModel testModel;

    @BeforeEach
    void setUp() {
        testModel = new DelayedSubmissionBusinessEmailModel(null, null, DELAY_IN_MINUTES);
    }

    @Test
    void getDelayedSubmissions() {
        assertThat(testModel.getDelayedSubmissions(), is(nullValue()));
    }

    @Test
    void setDelayedSubmissions() {
        final DelayedSubmissionBusinessModel businessModel =
            new DelayedSubmissionBusinessModel(null, null, null, null, null);
        final List<DelayedSubmissionBusinessModel> expected = Collections.singletonList(businessModel);

        testModel.setDelayedSubmissions(expected);

        assertThat(testModel.getDelayedSubmissions(), contains(businessModel));
    }

    @Test
    void getEmailAddress() {
        assertThat(testModel.getEmailAddress(), is(nullValue()));
    }

    @Test
    void setEmailAddress() {
        testModel.setEmailAddress("expected");
        assertThat(testModel.getEmailAddress(), is("expected"));
    }

    @Test
    void getNumberOfDelayedSubmissions() {
        assertThat(testModel.getNumberOfDelayedSubmissions(), is(0));

        final List<DelayedSubmissionBusinessModel> expected = Collections.singletonList(
            new DelayedSubmissionBusinessModel(null, null, null, null, null));

        testModel.setDelayedSubmissions(expected);

        assertThat(testModel.getNumberOfDelayedSubmissions(), is(1));
    }

    @Test
    void getDelayInMinutes() {
        assertThat(testModel.getDelayInMinutes(), is(DELAY_IN_MINUTES));
    }

    @Test
    void setDelayInMinutes() {
        testModel.setDelayInMinutes(DELAY_IN_MINUTES + 5);
        assertThat(testModel.getDelayInMinutes(), is(DELAY_IN_MINUTES + 5));
    }

    @Test
    void getDelayInHours() {
        assertThat(testModel.getDelayInHours(), is(DELAY_IN_MINUTES / 60));
    }

    @Test
    void setDelayInHours() {
        testModel.setDelayInHours(3);
        assertThat(testModel.getDelayInHours(), is(3));
    }

    @Test
    void testEquals() {
        EqualsVerifier.forClass(DelayedSubmissionBusinessEmailModel.class)
            .usingGetClass()
            .suppress(Warning.NONFINAL_FIELDS)
            .verify();
    }

    @Test
    void testToString() {
        assertThat(testModel.toString(),
            is("DelayedSubmissionBusinessEmailModel[delayInMinutes=60,delayedSubmissions=<null>,emailAddress=<null>]"));
    }

}