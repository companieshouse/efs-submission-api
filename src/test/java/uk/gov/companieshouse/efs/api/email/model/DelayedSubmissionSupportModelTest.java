package uk.gov.companieshouse.efs.api.email.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.LocalDateTime;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DelayedSubmissionSupportModelTest {
    private static final LocalDateTime NOW = LocalDateTime.now();
    private static final LocalDateTime INSTANT = NOW.plusMinutes(2);

    private DelayedSubmissionSupportModel testModel;

    @BeforeEach
    void setUp() {
        testModel = new DelayedSubmissionSupportModel("submissionId", "confirmationReference",
            NOW.toString());
    }

    @Test
    void getSubmissionId() {
        assertThat(testModel.getSubmissionId(), is("submissionId"));
    }

    @Test
    void setGetSubmissionId() {
        testModel.setSubmissionId("expected");
        assertThat(testModel.getSubmissionId(), is("expected"));
    }

    @Test
    void getConfirmationReference() {
        assertThat(testModel.getConfirmationReference(), is("confirmationReference"));
    }

    @Test
    void setConfirmationReference() {
        testModel.setConfirmationReference("expected");
        assertThat(testModel.getConfirmationReference(), is("expected"));
    }

    @Test
    void getSubmittedAt() {
        assertThat(testModel.getSubmittedAt(), is(NOW.toString()));
    }

    @Test
    void setSubmittedAt() {
        testModel.setSubmittedAt(INSTANT.toString());
        assertThat(testModel.getSubmittedAt(), is(INSTANT.toString()));
    }

    @Test
    void testEqualsAndHashcode() {
        EqualsVerifier.forClass(DelayedSubmissionSupportModel.class)
            .usingGetClass()
            .suppress(Warning.NONFINAL_FIELDS)
            .verify();
    }

    @Test
    void testToString() {
        assertThat(testModel.toString(),
            is("DelayedSubmissionSupportModel[confirmationReference=confirmationReference,submissionId=submissionId,submittedAt="
                + NOW.toString() + "]"));
    }
}