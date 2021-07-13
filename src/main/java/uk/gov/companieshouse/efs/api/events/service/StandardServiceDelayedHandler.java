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
import uk.gov.companieshouse.efs.api.email.model.DelayedSubmissionModel;
import uk.gov.companieshouse.efs.api.email.model.DelayedSubmissionSupportEmailModel;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.submissions.repository.SubmissionRepository;

@Component("standardServiceDelayedSubmissionHandler")
public class StandardServiceDelayedHandler implements DelayedSubmissionHandlerStrategy {
    static final String SUBMITTED_AT_SUPPORT_EMAIL_DATE_FORMAT = "dd/MM/yyyy HH:mm z";
    static final String SUBMITTED_AT_BUSINESS_EMAIL_DATE_FORMAT = "dd MMMM yyyy";
    static final ZoneId UTC_ZONE = ZoneId.of("UTC");
    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern(SUBMITTED_AT_BUSINESS_EMAIL_DATE_FORMAT).withZone(UTC_ZONE);

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
        List<DelayedSubmissionModel> supportModels = submissions.stream()
            .map(s -> DelayedSubmissionModel.newBuilder()
                .withSubmissionId(s.getId())
                .withConfirmationReference(s.getConfirmationReference())
                .withCustomerEmail(s.getPresenter().getEmail())
                .withCompanyNumber(s.getCompany().getCompanyNumber())
                .withSubmittedAt(Optional.ofNullable(s.getSubmittedAt())
                    .orElseGet(s::getCreatedAt)
                    .format(FORMATTER))
                .build())
            .collect(Collectors.toList());

        if (!supportModels.isEmpty()) {
            emailService.sendDelayedSubmissionSupportEmail(
                new DelayedSubmissionSupportEmailModel(supportModels,
                    (int) TimeUnit.HOURS.toMinutes(supportDelayInHours)));
            Map<String, List<DelayedSubmissionModel>> businessModels = submissions.stream()
                .filter(s -> s.getLastModifiedAt().isBefore(businessDelay))
                .map(s -> DelayedSubmissionModel.newBuilder()
                    .withConfirmationReference(s.getConfirmationReference())
                    .withCompanyNumber(s.getCompany().getCompanyNumber())
                    .withFormType(s.getFormDetails().getFormType())
                    .withCustomerEmail(s.getPresenter().getEmail())
                    .withSubmittedAt(Optional.ofNullable(s.getSubmittedAt())
                        .orElseGet(s::getCreatedAt)
                        .format(FORMATTER))
                    .build())
                .collect(Collectors.groupingBy(
                    m -> formCategoryToEmailAddressService.getEmailAddressForFormCategory(
                        m.getFormType())));
            businessModels.forEach((key, value) -> emailService.sendDelayedSubmissionBusinessEmail(
                new DelayedSubmissionBusinessEmailModel(value, key,
                    (int) TimeUnit.HOURS.toMinutes(businessDelayInHours))));
        }
    }
}
