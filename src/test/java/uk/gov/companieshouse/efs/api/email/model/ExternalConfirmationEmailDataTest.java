package uk.gov.companieshouse.efs.api.email.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.isA;

import java.util.Collections;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.efs.api.categorytemplates.model.CategoryTypeConstants;
import uk.gov.companieshouse.efs.api.submissions.model.Company;
import uk.gov.companieshouse.efs.api.submissions.model.Presenter;

@ExtendWith(MockitoExtension.class)
class ExternalConfirmationEmailDataTest {
    private static final Company COMPANY = new Company("10010010", "TEST COMPANY");
    private static final Presenter PRESENTER = new Presenter("presenter@email.com");
    private static final EmailFileDetails FILE_DETAILS = new EmailFileDetails();
    private ExternalConfirmationEmailData testData;

    @BeforeEach
    void setUp() {
        ExternalConfirmationEmailData.Builder builder = ExternalConfirmationEmailData.builder();
        testData = builder.withTo("recipient")
            .withSubject("subject")
            .withConfirmationReference("reference")
            .withCompany(COMPANY)
            .withFormType("form")
            .withTopLevelCategory(CategoryTypeConstants.CHANGE_OF_CONSTITUTION)
            .withPresenter(PRESENTER)
            .withFeeOnSubmission("fee")
            .withEmailFileDetailsList(Collections.singletonList(FILE_DETAILS))
            .build();
    }

    @Test
    void build() {
        assertThat(testData, isA(ExternalConfirmationEmailData.class));
    }

    @Test
    void getTo() {
        assertThat(testData.getTo(), is("recipient"));
    }

    @Test
    void getSubject() {
        assertThat(testData.getSubject(), is("subject"));
    }

    @Test
    void getConfirmationReference() {
        assertThat(testData.getConfirmationReference(), is("reference"));
    }

    @Test
    void getPresenter() {
        assertThat(testData.getPresenter(), is(PRESENTER));
    }

    @Test
    void getCompany() {
        assertThat(testData.getCompany(), is(COMPANY));
    }

    @Test
    void getFormType() {
        assertThat(testData.getFormType(), is("form"));
    }

    @Test
    void getTopLevelCategory() {
        assertThat(testData.getTopLevelCategory(), is(CategoryTypeConstants.CHANGE_OF_CONSTITUTION));
    }

    @Test
    void getEmailFileDetailsList() {
        assertThat(testData.getEmailFileDetailsList(), contains(FILE_DETAILS));
    }

    @Test
    void getFeeOnSubmission() {
        assertThat(testData.getFeeOnSubmission(), is("fee"));
    }

    @Test
    void testEqualsAndHashCode() {
        EqualsVerifier.forClass(ExternalConfirmationEmailData.class).usingGetClass().suppress(
            Warning.NONFINAL_FIELDS).verify();
    }
}