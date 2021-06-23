package uk.gov.companieshouse.efs.api.email.model;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
        assertThat(testData.getTo(), is("recipient"));
    }

    @Test
    void getSubject() {
        assertThat(testData.getSubject(), is("subject"));
    }

    @Test
    void getCompanyNumber() {
        assertThat(testData.getCompanyNumber(), is(COMPANY.getCompanyNumber()));
    }

    @Test
    void getCompanyName() {
        assertThat(testData.getCompanyName(), is(COMPANY.getCompanyName()));
    }

    @Test
    void getConfirmationReference() {
        assertThat(testData.getConfirmationReference(), is("reference"));
    }
    @Test
    void getFormType() {
        assertThat(testData.getFormType(), is("form"));
    }

    @Test
    void getRejectionDate() {
        assertThat(testData.getRejectionDate(), is("2020-02-20"));
    }

    @Test
    void getRejectReasons() {
        assertThat(testData.getRejectReasons(), contains("reason1", "reason2"));
    }

    @Test
    void testEqualsAndHashcode() {
        EqualsVerifier.forClass(ExternalRejectEmailData.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
    }

    @Test
    void deserializeWithBuilder() throws JsonProcessingException {
        String json =
            //@formatter:off
            "{" 
                + "\"to\":\"recipient\"," 
                + "\"subject\":\"subject\"," 
                + "\"companyNumber\":\"10010010\"," 
                + "\"companyName\":\"TEST COMPANY\"," 
                + "\"confirmationReference\":\"reference\"," 
                + "\"formType\":\"form\"," 
                + "\"rejectionDate\":\"2020-02-20\"," 
                + "\"rejectReasons\":[\"reason1\",\"reason2\"]" 
            + "}\n";
            //@formatter:on

        final ExternalRejectEmailData deserializedData =
            new ObjectMapper().readValue(json, ExternalRejectEmailData.class);
        
        assertThat(deserializedData, is(equalTo(testData)));
    }
}