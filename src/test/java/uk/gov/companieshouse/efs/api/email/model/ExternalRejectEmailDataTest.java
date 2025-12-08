package uk.gov.companieshouse.efs.api.email.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;

import java.util.Arrays;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.efs.api.submissions.model.Company;

@ExtendWith(MockitoExtension.class)
class ExternalRejectEmailDataTest {
    private static final Company COMPANY = new Company("10010010", "TEST COMPANY");

    private ExternalRejectEmailData testData;

    @BeforeEach
    void setUp() {
        final ExternalRejectEmailData.Builder builder = ExternalRejectEmailData.builder();

        testData = builder.withTo("recipient")
            .withSubject("subject")
            .withConfirmationReference("reference")
            .withCompanyName(COMPANY.getCompanyName())
            .withCompanyNumber(COMPANY.getCompanyNumber())
            .withFormType("form")
            .withRejectionDate("2020-02-20")
            .withRejectReasons(Arrays.asList("reason1", "reason2"))
            .build();
    }

    @Test
    void build() {
        assertThat(testData, isA(ExternalRejectEmailData.class));
    }

    @Test
    void getTo() {
        assertThat(testData.to(), is("recipient"));
    }

    @Test
    void getSubject() {
        assertThat(testData.subject(), is("subject"));
    }

    @Test
    void getCompanyNumber() {
        assertThat(testData.companyNumber(), is(COMPANY.getCompanyNumber()));
    }

    @Test
    void getCompanyName() {
        assertThat(testData.companyName(), is(COMPANY.getCompanyName()));
    }

    @Test
    void getConfirmationReference() {
        assertThat(testData.confirmationReference(), is("reference"));
    }
    @Test
    void getFormType() {
        assertThat(testData.formType(), is("form"));
    }

    @Test
    void getRejectionDate() {
        assertThat(testData.rejectionDate(), is("2020-02-20"));
    }

    @Test
    void getRejectReasons() {
        assertThat(testData.rejectReasons(), contains("reason1", "reason2"));
    }

    @Test
    void testEqualsAndHashcode() {
        EqualsVerifier.forClass(ExternalRejectEmailData.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
    }

}