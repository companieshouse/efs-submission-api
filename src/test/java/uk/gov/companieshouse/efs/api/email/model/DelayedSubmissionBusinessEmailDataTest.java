package uk.gov.companieshouse.efs.api.email.model;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.isA;

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

    private List<DelayedSubmissionBusinessModel> submissions;

    @BeforeEach
    void setUp() {
        submissions = Collections.emptyList();
        testData = new DelayedSubmissionBusinessEmailData("to", "subject", submissions, 3L);
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
    void setGetSubmissions() {
        final DelayedSubmissionBusinessModel expected =
            new DelayedSubmissionBusinessModel(null, null, null, null, null);
        testData.setSubmissions(Collections.singletonList(expected));

        assertThat(testData.getSubmissions(), contains(expected));
    }

    @Test
    void setGetDelayInDays() {
        testData.setDelayInDays(30L);

        assertThat(testData.getDelayInDays(), is(30L));
    }

    @Test
    void testHashCodeAndEquals() {
        EqualsVerifier.forClass(DelayedSubmissionBusinessEmailData.class)
            .usingGetClass()
            .suppress(Warning.NONFINAL_FIELDS)
            .verify();
    }

    @Test
    void builder() {
        assertThat(DelayedSubmissionBusinessEmailData.builder(),
            isA(DelayedSubmissionBusinessEmailData.Builder.class));
    }

    @Test
    void builtWithBuilder() {
        final DelayedSubmissionBusinessEmailData emailData =
            DelayedSubmissionBusinessEmailData.builder()
                .withTo("builder.to")
                .withSubject("builder.subject")
                .withDelayedSubmissions(submissions)
                .withDelayInDays(55L)
                .build();

        DelayedSubmissionBusinessEmailData expected =
            new DelayedSubmissionBusinessEmailData("builder.to", "builder.subject", submissions,
                55L);

        assertThat(emailData, is(equalTo(expected)));
    }
}