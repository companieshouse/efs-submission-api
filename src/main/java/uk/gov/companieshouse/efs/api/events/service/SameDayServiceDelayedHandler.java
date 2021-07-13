package uk.gov.companieshouse.efs.api.events.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
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

@Component("sameDayServiceDelayedSubmissionHandler")
public class SameDayServiceDelayedHandler implements DelayedSubmissionHandlerStrategy {
    static final String SUBMITTED_AT_SUPPORT_EMAIL_DATE_FORMAT = "dd/MM/yyyy HH:mm z";
    static final EnumSet<SubmissionStatus> DELAYED_STATUSES =
        EnumSet.of(SubmissionStatus.PROCESSING, SubmissionStatus.READY_TO_SUBMIT);
    static final ZoneId UTC_ZONE = ZoneId.of("UTC");
    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern(SUBMITTED_AT_SUPPORT_EMAIL_DATE_FORMAT).withZone(UTC_ZONE);
    private static final String SH19_SAMEDAY = "SH19_SAMEDAY";

    private SubmissionRepository repository;
    private EmailService emailService;
    private FormCategoryToEmailAddressService formCategoryToEmailAddressService;
    private int supportDelayInMinutes;
    private String internalSH19SameDayEmailAddress;

    @Autowired
    public SameDayServiceDelayedHandler(final SubmissionRepository repository,
        final EmailService emailService,
        final FormCategoryToEmailAddressService formCategoryToEmailAddressService,
        @Value("${submission.sameday.support.minutes}") final int supportDelayInMinutes,
        @Value("${internal.sharecapitalreduction.sh19.sameday.email.address}")
        final String internalSH19SameDayEmailAddress) {
        this.repository = repository;
        this.emailService = emailService;
        this.formCategoryToEmailAddressService = formCategoryToEmailAddressService;
        this.supportDelayInMinutes = supportDelayInMinutes;
        this.internalSH19SameDayEmailAddress = internalSH19SameDayEmailAddress;
    }

    @Override
    public final DelayedSubmissionHandlerContext.ServiceLevel getServiceLevel() {
        return DelayedSubmissionHandlerContext.ServiceLevel.SAMEDAY;
    }

    @Override
    public List<Submission> findDelayedSubmissions(final LocalDateTime handledAt) {
        LocalDateTime supportDelay = handledAt.minusMinutes(supportDelayInMinutes);

        return repository.findDelayedSameDaySubmissions(DELAYED_STATUSES, supportDelay);
    }

    @Override
    public void buildAndSendEmails(final List<Submission> submissions,
        final LocalDateTime handledAt) {
        List<DelayedSubmissionModel> models = submissions.stream()
            .map(s -> DelayedSubmissionModel.newBuilder()
                .withSubmissionId(s.getId())
                .withConfirmationReference(s.getConfirmationReference())
                .withCustomerEmail(s.getPresenter().getEmail())
                .withCompanyNumber(s.getCompany().getCompanyNumber())
                .withFormType(s.getFormDetails().getFormType())
                .withSubmittedAt(Optional.ofNullable(s.getSubmittedAt())
                    .orElseGet(s::getCreatedAt)
                    .format(FORMATTER))
                .build())
            .collect(Collectors.toList());

        if (!models.isEmpty()) {
            emailService.sendDelayedSH19SubmissionSupportEmail(
                new DelayedSubmissionSupportEmailModel(models, supportDelayInMinutes));
            emailService.sendDelayedSH19SubmissionBusinessEmail(
                new DelayedSubmissionBusinessEmailModel(models, internalSH19SameDayEmailAddress,
                    supportDelayInMinutes));
        }

    }
}
