package uk.gov.companieshouse.efs.api.events.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.efs.api.events.service.SameDayServiceDelayedHandler.DELAYED_STATUSES;
import static uk.gov.companieshouse.efs.api.events.service.StandardServiceDelayedHandler.SUBMITTED_AT_SUPPORT_EMAIL_DATE_FORMAT;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.efs.api.email.EmailService;
import uk.gov.companieshouse.efs.api.email.model.DelayedSubmissionSupportEmailModel;
import uk.gov.companieshouse.efs.api.email.model.DelayedSubmissionSupportModel;
import uk.gov.companieshouse.efs.api.submissions.model.Company;
import uk.gov.companieshouse.efs.api.submissions.model.Presenter;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.submissions.repository.SubmissionRepository;

@ExtendWith(MockitoExtension.class)
class SameDayServiceDelayedHandlerTest {
    private static final int SUPPORT_DELAY = 60;
    private static final LocalDateTime NOW = LocalDateTime.now();

    private SameDayServiceDelayedHandler testHandler;

    @Mock
    private SubmissionRepository repository;
    @Mock
    private EmailService emailService;
    @Mock
    private Submission submission;


    @BeforeEach
    void setUp() {
        testHandler = new SameDayServiceDelayedHandler(repository, emailService, SUPPORT_DELAY);
    }

    @Test
    void getServiceLevel() {
        assertThat(testHandler.getServiceLevel(),
            is(DelayedSubmissionHandlerContext.ServiceLevel.SAMEDAY));
    }

    @Test
    void findDelayedSubmissions() {
        // when
        testHandler.findDelayedSubmissions(NOW);

        // then
        verify(repository).findDelayedSameDaySubmissions(DELAYED_STATUSES,
            NOW.minusMinutes(SUPPORT_DELAY));
    }

    @Test
    void buildAndSendEmailsWhenNoneDelayed() {
        // when
        testHandler.buildAndSendEmails(Collections.emptyList(), NOW);

        // then
        verifyNoInteractions(emailService);
    }

    @Test
    void buildAndSendEmailsWhenSomeDelayedForSupport() {
        // given
        final LocalDateTime delayedFrom = NOW.minusMinutes(SUPPORT_DELAY);
        final List<Submission> submissions = Collections.singletonList(submission);

        when(submission.getId()).thenReturn("123abd");
        when(submission.getConfirmationReference()).thenReturn("345efg");
        when(submission.getSubmittedAt()).thenReturn(delayedFrom.minusSeconds(5));
        when(submission.getPresenter()).thenReturn(new Presenter("email"));
        when(submission.getCompany()).thenReturn(new Company("number", "name"));

        // when
        testHandler.buildAndSendEmails(submissions, NOW);

        // then
        verify(emailService).sendDelayedSH19SubmissionSupportEmail(
            new DelayedSubmissionSupportEmailModel(Collections.singletonList(
                new DelayedSubmissionSupportModel("123abd", "345efg", delayedFrom.minusSeconds(5)
                    .atZone(ZoneId.of("Europe/London"))
                    .format(DateTimeFormatter.ofPattern(SUBMITTED_AT_SUPPORT_EMAIL_DATE_FORMAT)),
                    submission.getPresenter().getEmail(),
                    submission.getCompany().getCompanyNumber())), SUPPORT_DELAY));
        verifyNoMoreInteractions(emailService);
    }

    @Test
    void buildAndSendEmailsWhenSomeDelayedForSupportAndSubmittedAtMissing() {
        // given
        final LocalDateTime delayedFrom = NOW.minusMinutes(SUPPORT_DELAY);
        final List<Submission> submissions = Collections.singletonList(submission);

        when(submission.getId()).thenReturn("123abd");
        when(submission.getConfirmationReference()).thenReturn("345efg");
        when(submission.getCreatedAt()).thenReturn(delayedFrom.minusSeconds(5));
        when(submission.getPresenter()).thenReturn(new Presenter("email"));
        when(submission.getCompany()).thenReturn(new Company("number", "name"));

        // when
        testHandler.buildAndSendEmails(submissions, NOW);

        // then
        verify(emailService).sendDelayedSH19SubmissionSupportEmail(
            new DelayedSubmissionSupportEmailModel(Collections.singletonList(
                new DelayedSubmissionSupportModel("123abd", "345efg", delayedFrom.minusSeconds(5)
                    .atZone(ZoneId.of("Europe/London"))
                    .format(DateTimeFormatter.ofPattern(SUBMITTED_AT_SUPPORT_EMAIL_DATE_FORMAT)),
                    submission.getPresenter().getEmail(),
                    submission.getCompany().getCompanyNumber())), SUPPORT_DELAY));
        verifyNoMoreInteractions(emailService);
    }

}