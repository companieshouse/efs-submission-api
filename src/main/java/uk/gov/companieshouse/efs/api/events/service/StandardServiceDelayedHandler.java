package uk.gov.companieshouse.efs.api.events.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus;
import uk.gov.companieshouse.efs.api.email.EmailService;
import uk.gov.companieshouse.efs.api.email.FormCategoryToEmailAddressService;
import uk.gov.companieshouse.efs.api.email.model.DelayedSubmissionBusinessEmailModel;
import uk.gov.companieshouse.efs.api.email.model.DelayedSubmissionBusinessModel;
import uk.gov.companieshouse.efs.api.email.model.DelayedSubmissionSupportEmailModel;
import uk.gov.companieshouse.efs.api.email.model.DelayedSubmissionSupportModel;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.submissions.repository.SubmissionRepository;

@Component("standardServiceDelayedSubmissionHandler")
public class StandardServiceDelayedHandler implements DelayedSubmissionHandlerStrategy {
    static final ZoneId UK_ZONE = ZoneId.of("Europe/London");
    static final String SUBMITTED_AT_SUPPORT_EMAIL_DATE_FORMAT = "dd/MM/yyyy HH:mm z";
    static final String SUBMITTED_AT_BUSINESS_EMAIL_DATE_FORMAT = "dd MMMM yyyy";

    private SubmissionRepository repository;
    private EmailService emailService;
    private FormCategoryToEmailAddressService formCategoryToEmailAddressService;
    private int supportDelayInHours;
    private int businessDelayInHours;

    @Autowired
    public StandardServiceDelayedHandler(final SubmissionRepository repository,
        final EmailService emailService,
        final FormCategoryToEmailAddressService formCategoryToEmailAddressService,
        @Value("${submission.processing.support.hours}") final int supportDelayInHours,
        @Value("${submission.processing.business.hours}") final int businessDelayInHours) {
        this.repository = repository;
        this.emailService = emailService;
        this.formCategoryToEmailAddressService = formCategoryToEmailAddressService;
        this.supportDelayInHours = supportDelayInHours;
        this.businessDelayInHours = businessDelayInHours;
    }

    @Override
    public final DelayedSubmissionHandlerContext.ServiceLevel getServiceLevel() {
        return DelayedSubmissionHandlerContext.ServiceLevel.STANDARD;
    }

    @Override
    public List<Submission> findDelayedSubmissions(final LocalDateTime handledAt) {
        LocalDateTime supportDelay = handledAt.minusHours(supportDelayInHours);

        return repository.findDelayedSubmissions(SubmissionStatus.PROCESSING, supportDelay);
    }

    @Override
    public void buildAndSendEmails(final List<Submission> submissions,
        final LocalDateTime handledAt) {
        LocalDateTime businessDelay = handledAt.minusHours(businessDelayInHours);
        List<DelayedSubmissionSupportModel> supportModels = submissions.stream()
            .map(submission -> new DelayedSubmissionSupportModel(submission.getId(),
                submission.getConfirmationReference(),
                Optional.ofNullable(submission.getSubmittedAt())
                    .orElseGet(submission::getCreatedAt)
                    .atZone(UK_ZONE)
                    .format(DateTimeFormatter.ofPattern(SUBMITTED_AT_SUPPORT_EMAIL_DATE_FORMAT)),
                submission.getPresenter().getEmail(), submission.getCompany().getCompanyNumber()))
            .collect(Collectors.toList());

        if (!supportModels.isEmpty()) {
            emailService.sendDelayedSubmissionSupportEmail(
                new DelayedSubmissionSupportEmailModel(supportModels,
                    (int) TimeUnit.HOURS.toMinutes(supportDelayInHours)));
            Map<String, List<DelayedSubmissionBusinessModel>> delayedSubmissionBusinessModels =
                submissions.stream()
                    .filter(submission -> submission.getLastModifiedAt().isBefore(businessDelay))
                    .map(submission -> new DelayedSubmissionBusinessModel(
                        submission.getConfirmationReference(),
                        submission.getCompany().getCompanyNumber(),
                        submission.getFormDetails().getFormType(),
                        submission.getPresenter().getEmail(),
                        Optional.ofNullable(submission.getSubmittedAt())
                            .orElseGet(submission::getCreatedAt)
                            .atZone(UK_ZONE)
                            .format(DateTimeFormatter.ofPattern(
                                SUBMITTED_AT_BUSINESS_EMAIL_DATE_FORMAT))))
                    .collect(Collectors.groupingBy(
                        delayedSubmissionModel -> formCategoryToEmailAddressService.getEmailAddressForFormCategory(
                            delayedSubmissionModel.getFormType())));
            delayedSubmissionBusinessModels.forEach(
                (key, value) -> emailService.sendDelayedSubmissionBusinessEmail(
                    new DelayedSubmissionBusinessEmailModel(value, key,
                        (int) TimeUnit.HOURS.toMinutes(businessDelayInHours))));
        }
    }
}
