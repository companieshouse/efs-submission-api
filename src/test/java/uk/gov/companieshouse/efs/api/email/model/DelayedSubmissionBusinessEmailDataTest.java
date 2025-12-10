package uk.gov.companieshouse.efs.api.email.model;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Collections;
import java.util.List;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DelayedSubmissionBusinessEmailDataTest {
    private DelayedSubmissionBusinessEmailData testData;

    @BeforeEach
    void setUp() {
        final List<DelayedSubmissionBusinessModel> submissions = Collections.emptyList();
        testData = new DelayedSubmissionBusinessEmailData("to", "subject", submissions, 3L);
    }

    @Test
    void testHashCodeAndEquals() {
        EqualsVerifier.forClass(DelayedSubmissionBusinessEmailData.class)
            .usingGetClass()
            .suppress(Warning.NONFINAL_FIELDS)
            .verify();
    }

    //String confirmationReference, String companyNumber, String formType, String email, String submissionDate

    @Test
    void testConstructorAndAccessors() {
        final DelayedSubmissionBusinessModel submissionBusinessModel = new DelayedSubmissionBusinessModel("confRef", "compNum", "formType", "email", "2024-01-01");
        final List<DelayedSubmissionBusinessModel> submissions = Collections.singletonList(submissionBusinessModel);
        final DelayedSubmissionBusinessEmailData data = new DelayedSubmissionBusinessEmailData("to", "subject", submissions, 5L);
        assertThat(data.to(), is("to"));
        assertThat(data.subject(), is("subject"));
        assertThat(data.submissions(), is(submissions));
        assertThat(data.delayInDays(), is(5L));
    }

    @Test
    void testToStringContainsAllFields() {
        final String result = testData.toString();
        assertThat(result, is(equalTo("DelayedSubmissionBusinessEmailData[to=to, subject=subject, submissions=[], delayInDays=3]")));
    }

    @Test
    void testNullFields() {
        final DelayedSubmissionBusinessEmailData data = new DelayedSubmissionBusinessEmailData(null, null, null, 0L);
        assertThat(data.to(), is((String) null));
        assertThat(data.subject(), is((String) null));
        assertThat(data.submissions(), is((List<DelayedSubmissionBusinessModel>) null));
        assertThat(data.delayInDays(), is(0L));
    }

    @Test
    void testImmutability() {
        // Records are immutable, so we cannot change fields after construction
        // This test is just a compile-time guarantee, but we assert the value remains unchanged
        assertThat(testData.delayInDays(), is(3L));
    }

    @Test
    void testEdgeCases() {
        final DelayedSubmissionBusinessEmailData data = new DelayedSubmissionBusinessEmailData("", "", Collections.emptyList(), Long.MAX_VALUE);
        assertThat(data.to(), is(""));
        assertThat(data.subject(), is(""));
        assertThat(data.submissions(), is(Collections.emptyList()));
        assertThat(data.delayInDays(), is(Long.MAX_VALUE));
    }

}