package uk.gov.companieshouse.efs.api.events.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.efs.api.events.service.StandardServiceDelayedHandler.SUBMITTED_AT_BUSINESS_EMAIL_DATE_FORMAT;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus;
import uk.gov.companieshouse.efs.api.email.EmailService;
import uk.gov.companieshouse.efs.api.email.FormCategoryToEmailAddressService;
import uk.gov.companieshouse.efs.api.email.model.DelayedSubmissionBusinessEmailModel;
import uk.gov.companieshouse.efs.api.email.model.DelayedSubmissionModel;
import uk.gov.companieshouse.efs.api.email.model.DelayedSubmissionSupportEmailModel;
import uk.gov.companieshouse.efs.api.submissions.model.Company;
import uk.gov.companieshouse.efs.api.submissions.model.FormDetails;
import uk.gov.companieshouse.efs.api.submissions.model.Presenter;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.submissions.repository.SubmissionRepository;

@ExtendWith(MockitoExtension.class)
class StandardServiceDelayedHandlerTest {
    private static final int SUPPORT_DELAY = 6;
    private static final int BUSINESS_DELAY = 72;
    private static final LocalDateTime NOW = LocalDateTime.now();
    static final ZoneId UTC_ZONE = ZoneId.of("UTC");
    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern(SUBMITTED_AT_BUSINESS_EMAIL_DATE_FORMAT).withZone(UTC_ZONE);

    private StandardServiceDelayedHandler testHandler;

    @Mock
    private SubmissionRepository repository;
    @Mock
    private EmailService emailService;
    @Mock
    private FormCategoryToEmailAddressService formCategoryToEmailAddressService;
    @Mock
    private Submission submission;
    private static final DateTimeFormatter BUSINESS_FORMATTER =
        DateTimeFormatter.ofPattern(SUBMITTED_AT_BUSINESS_EMAIL_DATE_FORMAT);


    @BeforeEach
    void setUp() {
        testHandler = new StandardServiceDelayedHandler(repository, emailService,
            formCategoryToEmailAddressService, SUPPORT_DELAY, BUSINESS_DELAY);
    }

    @Test
    void getServiceLevel() {
        assertThat(testHandler.getServiceLevel(),
            is(DelayedSubmissionHandlerContext.ServiceLevel.STANDARD));
    }

    @Test
    void findDelayedSubmissions() {
        // when
        testHandler.findDelayedSubmissions(NOW);

        // then
        verify(repository).findDelayedSubmissions(SubmissionStatus.PROCESSING,
            NOW.minusHours(SUPPORT_DELAY));
    }

    @Test
    void buildAndSendEmailsWhenNoneDelayed() {
        // when
        testHandler.buildAndSendEmails(Collections.emptyList(), NOW);

        // then
        verifyNoInteractions(emailService, formCategoryToEmailAddressService);
    }


    @Test
    void buildAndSendEmailsWhenSomeDelayedForSupport() {
        // given
        final LocalDateTime delayedFrom = NOW.minusHours(SUPPORT_DELAY);
        final List<Submission> submissions = Collections.singletonList(submission);

        when(submission.getId()).thenReturn("123abd");
        when(submission.getConfirmationReference()).thenReturn("345efg");
        when(submission.getLastModifiedAt()).thenReturn(delayedFrom);
        when(submission.getSubmittedAt()).thenReturn(delayedFrom.minusSeconds(5));
        when(submission.getPresenter()).thenReturn(new Presenter("email"));
        when(submission.getCompany()).thenReturn(new Company("number", "name"));

        // when
        testHandler.buildAndSendEmails(submissions, NOW);

        // then
        verify(emailService).sendDelayedSubmissionSupportEmail(
            new DelayedSubmissionSupportEmailModel(Collections.singletonList(
                createModel(submission, submission.getFormDetails().getFormType(),
                    delayedFrom.minusSeconds(5), FORMATTER)), SUPPORT_DELAY * 60));
        verifyNoMoreInteractions(emailService);
        verifyNoInteractions(formCategoryToEmailAddressService);
    }

    @Test
    void buildAndSendEmailsWhenSomeDelayedForSupportAndSubmittedAtMissing() {
        // given
        final LocalDateTime delayedFrom = NOW.minusHours(SUPPORT_DELAY);
        final List<Submission> submissions = Collections.singletonList(submission);

        when(submission.getId()).thenReturn("123abd");
        when(submission.getConfirmationReference()).thenReturn("345efg");
        when(submission.getLastModifiedAt()).thenReturn(delayedFrom);
        when(submission.getCreatedAt()).thenReturn(delayedFrom.minusSeconds(5));
        when(submission.getPresenter()).thenReturn(new Presenter("email"));
        when(submission.getCompany()).thenReturn(new Company("number", "name"));

        // when
        testHandler.buildAndSendEmails(submissions, NOW);

        // then
        verify(emailService).sendDelayedSubmissionSupportEmail(
            new DelayedSubmissionSupportEmailModel(Collections.singletonList(
                createModel(submission, submission.getFormDetails().getFormType(),
                    delayedFrom.minusSeconds(5), FORMATTER)), SUPPORT_DELAY * 60));
        verifyNoMoreInteractions(emailService);
        verifyNoInteractions(formCategoryToEmailAddressService);
    }

    @Test
    void buildAndSendEmailsWhenSomeDelayedForSupportAndBusiness() {
        // given
        final LocalDateTime delayedFrom = NOW.minusHours(BUSINESS_DELAY);
        final List<Submission> submissions = Collections.singletonList(submission);

        when(submission.getId()).thenReturn("123abd");
        when(submission.getConfirmationReference()).thenReturn("345efg");
        when(submission.getCompany()).thenReturn(new Company("00000007", "RITCHIE GROUP"));
        when(submission.getFormDetails()).thenReturn(
            FormDetails.builder().withFormType("CC01").build());
        when(submission.getPresenter()).thenReturn(new Presenter("demo@ch.gov.uk"));
        when(submission.getLastModifiedAt()).thenReturn(delayedFrom.minusSeconds(1));
        when(submission.getSubmittedAt()).thenReturn(delayedFrom.minusSeconds(5));
        when(formCategoryToEmailAddressService.getEmailAddressForFormCategory("CC01")).thenReturn(
            "cc@ch.gov.uk");

        // when
        testHandler.buildAndSendEmails(submissions, NOW);

        // then
        verify(emailService).sendDelayedSubmissionSupportEmail(
            new DelayedSubmissionSupportEmailModel(Collections.singletonList(
                createModel(submission, submission.getFormDetails().getFormType(),
                    delayedFrom.minusSeconds(5), FORMATTER)), SUPPORT_DELAY * 60));
        verify(emailService).sendDelayedSubmissionBusinessEmail(
            new DelayedSubmissionBusinessEmailModel(Collections.singletonList(
                createModel(submission, submission.getFormDetails().getFormType(),
                    delayedFrom.minusSeconds(5),
                    DateTimeFormatter.ofPattern(SUBMITTED_AT_BUSINESS_EMAIL_DATE_FORMAT))),
                "cc@ch.gov.uk", BUSINESS_DELAY * 60));
        verify(formCategoryToEmailAddressService).getEmailAddressForFormCategory("CC01");
        verifyNoMoreInteractions(emailService, formCategoryToEmailAddressService);
    }

    @Test
    void buildAndSendEmailsWhenSomeDelayedForSupportAndBusinessAndSubmittedAtMissing() {
        //given
        final LocalDateTime delayedFrom = NOW.minusHours(BUSINESS_DELAY);
        final List<Submission> submissions = Collections.singletonList(submission);

        when(submission.getId()).thenReturn("123abd");
        when(submission.getConfirmationReference()).thenReturn("345efg");
        when(submission.getCompany()).thenReturn(new Company("00000007", "RITCHIE GROUP"));
        when(submission.getFormDetails()).thenReturn(
            FormDetails.builder().withFormType("CC01").build());
        when(submission.getPresenter()).thenReturn(new Presenter("demo@ch.gov.uk"));
        when(submission.getLastModifiedAt()).thenReturn(delayedFrom.minusSeconds(1));
        when(submission.getSubmittedAt()).thenReturn(delayedFrom.minusSeconds(5));
        when(formCategoryToEmailAddressService.getEmailAddressForFormCategory("CC01")).thenReturn(
            "cc@ch.gov.uk");

        // when
        testHandler.buildAndSendEmails(submissions, NOW);

        // then
        verify(emailService).sendDelayedSubmissionSupportEmail(
            new DelayedSubmissionSupportEmailModel(Collections.singletonList(
                createModel(submission, submission.getFormDetails().getFormType(),
                    delayedFrom.minusSeconds(5), FORMATTER)), SUPPORT_DELAY * 60));
        verify(emailService).sendDelayedSubmissionBusinessEmail(
            new DelayedSubmissionBusinessEmailModel(Collections.singletonList(
                createModel(submission, submission.getFormDetails().getFormType(),
                    delayedFrom.minusSeconds(5),
                    DateTimeFormatter.ofPattern(SUBMITTED_AT_BUSINESS_EMAIL_DATE_FORMAT))),
                "cc@ch.gov.uk", BUSINESS_DELAY * 60));
        verify(formCategoryToEmailAddressService).getEmailAddressForFormCategory("CC01");
        verifyNoMoreInteractions(emailService, formCategoryToEmailAddressService);
    }

    @Test
    void buildAndSendEmailsWhenSomeDelayedForSupportAndBusinessToMultipleUnitsIfDifferentSubmissionCategories() {
        //given
        final LocalDateTime delayedFrom = NOW.minusHours(BUSINESS_DELAY);
        final List<Submission> submissions = Arrays.asList(submission, submission, submission);

        when(submission.getId()).thenReturn("123abd");
        when(submission.getConfirmationReference()).thenReturn("345efg");
        when(submission.getCompany()).thenReturn(new Company("00000007", "RITCHIE GROUP"));
        when(submission.getFormDetails()).thenReturn(
            FormDetails.builder().withFormType("CC01").build())
            .thenReturn(FormDetails.builder().withFormType("RP03").build())
            .thenReturn(FormDetails.builder().withFormType("CC03").build());
        when(submission.getPresenter()).thenReturn(new Presenter("demo@ch.gov.uk"));
        when(submission.getLastModifiedAt()).thenReturn(delayedFrom.minusSeconds(1));
        when(submission.getSubmittedAt()).thenReturn(delayedFrom.minusSeconds(5));
        when(formCategoryToEmailAddressService.getEmailAddressForFormCategory("CC01")).thenReturn(
            "cc@ch.gov.uk");
        when(formCategoryToEmailAddressService.getEmailAddressForFormCategory("CC03")).thenReturn(
            "cc@ch.gov.uk");
        when(formCategoryToEmailAddressService.getEmailAddressForFormCategory("RP03")).thenReturn(
            "rp@ch.gov.uk");

        // when
        testHandler.buildAndSendEmails(submissions, NOW);

        // then
        final DelayedSubmissionModel supportModel =
            createModel(submission, submission.getFormDetails().getFormType(),
                delayedFrom.minusSeconds(5), FORMATTER);

        verify(emailService).sendDelayedSubmissionSupportEmail(
            new DelayedSubmissionSupportEmailModel(
                Arrays.asList(supportModel, supportModel, supportModel), SUPPORT_DELAY * 60));
        verify(emailService).sendDelayedSubmissionBusinessEmail(
            new DelayedSubmissionBusinessEmailModel(Arrays.asList(
                createModel(submission, "CC01", delayedFrom.minusSeconds(5), BUSINESS_FORMATTER),
                createModel(submission, "CC03", delayedFrom.minusSeconds(5), BUSINESS_FORMATTER)),
                "cc@ch.gov.uk", BUSINESS_DELAY * 60));
        verify(emailService).sendDelayedSubmissionBusinessEmail(
            new DelayedSubmissionBusinessEmailModel(Collections.singletonList(
                createModel(submission, "RP03", delayedFrom.minusSeconds(5), BUSINESS_FORMATTER)),
                "rp@ch.gov.uk", BUSINESS_DELAY * 60));
        verify(formCategoryToEmailAddressService).getEmailAddressForFormCategory("CC01");
        verify(formCategoryToEmailAddressService).getEmailAddressForFormCategory("RP03");
        verifyNoMoreInteractions(emailService, formCategoryToEmailAddressService);
    }

    private DelayedSubmissionModel createModel(final Submission submission, final String formType,
        final LocalDateTime submittedAt, final DateTimeFormatter formatter) {
        return DelayedSubmissionModel.newBuilder()
            .withSubmissionId(submission.getId())
            .withConfirmationReference(submission.getConfirmationReference())
            .withSubmittedAt(submittedAt.format(formatter))
            .withCustomerEmail(submission.getPresenter().getEmail())
            .withCompanyNumber(submission.getCompany().getCompanyNumber())
            .withFormType(formType)
            .build();
    }
}