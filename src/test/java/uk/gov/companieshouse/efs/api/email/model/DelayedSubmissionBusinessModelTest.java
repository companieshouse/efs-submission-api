package uk.gov.companieshouse.efs.api.email.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DelayedSubmissionBusinessModelTest {
    private DelayedSubmissionBusinessModel testModel;

    @BeforeEach
    void setUp() {
        testModel = new DelayedSubmissionBusinessModel(null, null, null, null, null);
    }

    @Test
    void setGetConfirmationReference() {
        testModel.setConfirmationReference("expected");
        assertThat(testModel.getConfirmationReference(), is("expected"));
    }

    @Test
    void setGetCompanyNumber() {
        testModel.setCompanyNumber("expected");
        assertThat(testModel.getCompanyNumber(), is("expected"));
    }

    @Test
    void setGetFormType() {
        testModel.setFormType("expected");
        assertThat(testModel.getFormType(), is("expected"));
    }

    @Test
    void setGetEmail() {
        testModel.setEmail("expected");
        assertThat(testModel.getEmail(), is("expected"));
    }

    @Test
    void setGetSubmissionDate() {
        testModel.setSubmissionDate("expected");
        assertThat(testModel.getSubmissionDate(), is("expected"));
    }

    @Test
    void testEquals() {
        EqualsVerifier.forClass(DelayedSubmissionBusinessModel.class)
            .usingGetClass()
            .suppress(Warning.NONFINAL_FIELDS)
            .verify();
    }

    @Test
    void testToString() {
        assertThat(testModel.toString(),
            is("DelayedSubmissionBusinessModel[companyNumber=<null>," 
                + "confirmationReference=<null>,email=<null>,formType=<null>," 
                + "submissionDate=<null>]"));
    }
}