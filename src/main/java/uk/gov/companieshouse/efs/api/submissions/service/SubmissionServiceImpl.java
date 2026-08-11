package uk.gov.companieshouse.efs.api.submissions.service;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.efs.submissions.CompanyApi;
import uk.gov.companieshouse.api.model.efs.submissions.FileConversionStatus;
import uk.gov.companieshouse.api.model.efs.submissions.FileListApi;
import uk.gov.companieshouse.api.model.efs.submissions.FormTypeApi;
import uk.gov.companieshouse.api.model.efs.submissions.PresenterApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionResponseApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus;
import uk.gov.companieshouse.api.model.paymentsession.SessionListApi;
import uk.gov.companieshouse.efs.api.email.EmailService;
import uk.gov.companieshouse.efs.api.email.model.ExternalNotificationEmailModel;
import uk.gov.companieshouse.efs.api.formtemplates.service.FormTemplateService;
import uk.gov.companieshouse.efs.api.payment.PaymentClose;
import uk.gov.companieshouse.efs.api.payment.service.PaymentTemplateService;
import uk.gov.companieshouse.efs.api.submissions.mapper.CompanyMapper;
import uk.gov.companieshouse.efs.api.submissions.mapper.FileDetailsMapper;
import uk.gov.companieshouse.efs.api.submissions.mapper.PresenterMapper;
import uk.gov.companieshouse.efs.api.submissions.mapper.SubmissionMapper;
import uk.gov.companieshouse.efs.api.submissions.model.FileDetails;
import uk.gov.companieshouse.efs.api.submissions.model.FormDetails;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.submissions.repository.SubmissionRepository;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionIncorrectStateException;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionNotFoundException;
import uk.gov.companieshouse.efs.api.submissions.validator.Validator;
import uk.gov.companieshouse.efs.api.submissions.validator.exception.SubmissionValidationException;
import uk.gov.companieshouse.efs.api.util.CurrentTimestampGenerator;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Service
public class SubmissionServiceImpl implements SubmissionService {

    private static final Logger LOGGER = LoggerFactory.getLogger("efs-submission-api");
    public static final String SUBMISSION_STATUS_MSG =
        "Updated submission status to %s for submission with id: [%s] at [%s]";
    public static final String SUBMITTED_STATUS_MSG =
        "Updated status SUBMITTED at [%s] for submission with id: [%s]";
    public static final ImmutableSet<SubmissionStatus> UPDATABLE_STATUSES =
        Sets.immutableEnumSet(SubmissionStatus.OPEN, SubmissionStatus.PAYMENT_REQUIRED, SubmissionStatus.PAYMENT_FAILED);

    public static final ImmutableSet<SubmissionStatus> VALIDATABLE_STATUSES =
        Sets.immutableEnumSet(SubmissionStatus.OPEN, SubmissionStatus.PAYMENT_REQUIRED, SubmissionStatus.PAYMENT_FAILED,
            SubmissionStatus.SUBMITTED);

    private final SubmissionRepository submissionRepository;
    private final SubmissionMapper submissionMapper;
    private final PresenterMapper presenterMapper;
    private final CompanyMapper companyMapper;
    private final FileDetailsMapper fileDetailsMapper;
    private final CurrentTimestampGenerator timestampGenerator;
    private final ConfirmationReferenceGeneratorService confirmationReferenceGenerator;
    private final FormTemplateService formTemplateService;
    private final PaymentTemplateService paymentTemplateService;
    private final EmailService emailService;
    private final Validator<Submission> validator;
    private final Clock clock;


    public SubmissionServiceImpl(final SubmissionRepository submissionRepository,
        final SubmissionMapper submissionMapper, final PresenterMapper presenterMapper,
        final CompanyMapper companyMapper, final FileDetailsMapper fileDetailsMapper,
        final CurrentTimestampGenerator timestampGenerator,
        final ConfirmationReferenceGeneratorService confirmationReferenceGenerator,
        final FormTemplateService formTemplateService, final PaymentTemplateService paymentTemplateService,
        final EmailService emailService, final Validator<Submission> validator, final Clock clock) {
        this.submissionRepository = submissionRepository;
        this.submissionMapper = submissionMapper;
        this.presenterMapper = presenterMapper;
        this.companyMapper = companyMapper;
        this.fileDetailsMapper = fileDetailsMapper;
        this.timestampGenerator = timestampGenerator;
        this.confirmationReferenceGenerator = confirmationReferenceGenerator;
        this.formTemplateService = formTemplateService;
        this.paymentTemplateService = paymentTemplateService;
        this.emailService = emailService;
        this.validator = validator;
        this.clock = clock;
    }

    @Override
    public SubmissionApi readSubmission(final String id) {
        final var submission = submissionRepository.read(id);
        if (submission != null) {
            return submissionMapper.map(submission);
        } else {
            return null;
        }
    }

    @Override
    public SubmissionResponseApi createSubmission(final PresenterApi presenterApi) {
        LOGGER.debug("Attempting to create a submission with email: [%s]".formatted(presenterApi.getEmail()));
        final var presenter = presenterMapper.map(presenterApi);
        final var timestamp = timestampGenerator.generateTimestamp();
        final var confirmRef = confirmationReferenceGenerator.generateId();
        final var submission = Submission.builder().withConfirmationReference(confirmRef).withPresenter(presenter)
                                   .withStatus(SubmissionStatus.OPEN).withCreatedAt(timestamp).withLastModifiedAt(timestamp).build();
        submissionRepository.create(submission);
        LOGGER.debug(
            "Successfully created a submission with email: [%s] and id: [%s] at [%s]".formatted(
                presenterApi.getEmail(), submission.getId(),
                DateTimeFormatter.ISO_INSTANT.format(timestamp.atZone(ZoneId.of("UTC")))));
        return new SubmissionResponseApi(submission.getId());
    }

    @Override
    public SubmissionResponseApi updateSubmissionWithCompany(final String id, final CompanyApi companyApi) {
        LOGGER.debug("Attempting to update company details for submission with id: [%s]".formatted(id));
        final var updatedSubmission = Submission.builder(this.getSubmissionWithCheckedStatus(id, UPDATABLE_STATUSES)).withCompany(companyMapper.map(companyApi)).build();
        submissionRepository.updateSubmission(updatedSubmission);
        LOGGER.debug("Successfully updated company details for submission with id: [%s]".formatted(id));
        return new SubmissionResponseApi(id);
    }

    @Override
    public SubmissionResponseApi updateSubmissionWithForm(final String id, final FormTypeApi formApi) {
        LOGGER.debug("Attempting to update form type for submission with id: [%s]".formatted(id));
        final var submission = this.getSubmissionWithCheckedStatus(id, UPDATABLE_STATUSES);
        var formDetails = submission.getFormDetails();
        final var formType = formApi.getFormType();
        if (formDetails == null) {
            formDetails = FormDetails.builder().withFormType(formType).build();
        } else {
            formDetails.setFormType(formType);
        }
        LOGGER.debug("Attempting to update fee for submission with id: [%s]".formatted(id));
        final var updatedSubmission = Submission.builder(submission).withFeeOnSubmission(getPaymentCharge(formType)).withFormDetails(formDetails).build();
        submissionRepository.updateSubmission(updatedSubmission);
        LOGGER.debug("Successfully updated form type for submission with id: [%s]".formatted(id));
        return new SubmissionResponseApi(id);
    }

    @Override
    public SubmissionResponseApi clearSubmissionFiles(final String id) {
        LOGGER.debug("Attempting to clear file details for submission with id: [%s]".formatted(id));
        final var submission = this.getSubmissionWithCheckedStatus(id, UPDATABLE_STATUSES);
        final var formDetails = submission.getFormDetails();
        if (formDetails != null) {
            formDetails.setFileDetailsList(null);
        }
        final var updatedSubmission = Submission.builder(submission).withFormDetails(formDetails).build();
        submissionRepository.updateSubmission(updatedSubmission);
        LOGGER.debug("Successfully cleared file details for submission with id: [%s]".formatted(id));
        return new SubmissionResponseApi(id);
    }

    @Override
    public SubmissionResponseApi updateSubmissionWithFileDetails(final String id, final FileListApi fileListApi) {
        LOGGER.debug("Attempting to update file details for submission with id: [%s]".formatted(id));
        var formDetails = this.getSubmissionWithCheckedStatus(id, UPDATABLE_STATUSES).getFormDetails();
        if (formDetails == null) {
            formDetails = FormDetails.builder().withFileDetailsList(fileDetailsMapper.map(fileListApi)).build();
        } else {
            formDetails.setFileDetailsList(fileDetailsMapper.map(fileListApi));
        }
        final var updatedSubmission = Submission.builder(this.getSubmissionWithCheckedStatus(id, UPDATABLE_STATUSES)).withFormDetails(formDetails).build();
        submissionRepository.updateSubmission(updatedSubmission);
        LOGGER.debug("Successfully updated file details for submission with id: [%s]".formatted(id));

        return new SubmissionResponseApi(id);
    }

    @Override
    public SubmissionResponseApi updateSubmissionWithPaymentSessions(final String id,
        final SessionListApi paymentSessions) {
        LOGGER.debug(
            "Attempting to update payment sessions for submission with id: [%s]".formatted(
                id));
        final var updatedSubmission = Submission.builder(this.getSubmissionWithCheckedStatus(id, UPDATABLE_STATUSES)).withPaymentSessions(paymentSessions).build();
        submissionRepository.updateSubmission(updatedSubmission);

        return new SubmissionResponseApi(id);
    }

    @Override
    public SubmissionResponseApi updateSubmissionWithPaymentOutcome(final String id,
        final PaymentClose paymentClose) {
        final var submission = getSubmissionWithCheckedStatus(id, UPDATABLE_STATUSES);
        final var status = submission.getStatus();

        setPaymentSessionStatus(submission, paymentClose);
        LOGGER.debug("Updating submission status %s for submission with id: [%s]".formatted(status, submission.getId()));

        if (SubmissionStatus.PAYMENT_REQUIRED == submission.getStatus()) {
            final SubmissionStatus resultStatus;

            if (paymentClose.isPaid()) {
                resultStatus = SubmissionStatus.SUBMITTED;
            } else if (paymentClose.isFailed()){
                resultStatus = SubmissionStatus.PAYMENT_FAILED;
            } else {
                return new SubmissionResponseApi(id);
            }

            final var lastModified = timestampGenerator.generateTimestamp();
            final Submission updatedSubmission;
            var builder = Submission.builder(submission).withStatus(resultStatus).withLastModifiedAt(lastModified);
            if (resultStatus == SubmissionStatus.SUBMITTED) {
                builder = builder.withSubmittedAt(lastModified);
            }
            updatedSubmission = builder.build();
            submissionRepository.updateSubmission(updatedSubmission);
            LOGGER.debug(SUBMISSION_STATUS_MSG.formatted(resultStatus, submission.getId(),
                DateTimeFormatter.ISO_INSTANT.format(lastModified.atZone(ZoneId.of("UTC")))));
        }

        return new SubmissionResponseApi(id);
    }

    private void setPaymentSessionStatus(final Submission submission,
        final PaymentClose paymentClose) {
        LOGGER.debug("Attempting to update payment session outcome for submission with id: [%s]".formatted(
            submission.getId()));

        final var matchedSession =
            Optional.ofNullable(submission.getPaymentSessions()).stream().flatMap(Collection::stream)
                .filter(
                    s -> Objects.equals(s.getSessionId(), paymentClose.getPaymentReference()))
                .findFirst();
        matchedSession.orElseThrow(
            () -> new SubmissionIncorrectStateException("payment reference not matched"))
            .setSessionStatus(paymentClose.getStatus());
    }

    @Override
    public SubmissionResponseApi completeSubmission(final String id)
        throws SubmissionValidationException {

        LOGGER.debug("Attempting to complete submission for id: [%s]".formatted(id));

        var submission = this.getSubmissionWithCheckedStatus(id, VALIDATABLE_STATUSES);

        // check submission mandatory fields (validator)
        try {
            validator.validate(submission);
        } catch (final SubmissionValidationException ve) {
            LOGGER.info(ve.getMessage());

            final var debug = getDebugMap(id);

            debug.put("exceptionMessage", ve.getMessage());
            LOGGER.errorContext(id, "Submission invalid ", null, debug);
            throw ve;
        }

        if (submission.getFeeOnSubmission() == null) {

            final var updatedSubmission = progressSubmissionStatusToSubmitted(submission);

            updateSubmission(updatedSubmission);
            LOGGER.debug("Successfully completed submission for id: [%s]".formatted(id));

            submission = getSubmissionWithCheckedStatus(id, Set.of(SubmissionStatus.SUBMITTED));

            emailService.sendExternalConfirmation(new ExternalNotificationEmailModel(submission));
        }

        return new SubmissionResponseApi(id);
    }

    private Submission progressSubmissionStatusToSubmitted(final Submission submission) {
        final var status = submission.getStatus();

        LOGGER.debug("Updating submission status %s for submission with id: [%s]".formatted(
            status, submission.getId()));
        return updateAsSubmitted(submission);
    }

    private Submission updateAsSubmitted(final Submission submission) {
        final var lastModified = timestampGenerator.generateTimestamp();
        final var updatedSubmission = Submission.builder(submission).withStatus(SubmissionStatus.SUBMITTED).withSubmittedAt(lastModified).build();
        LOGGER.debug(
            SUBMISSION_STATUS_MSG.formatted(SubmissionStatus.SUBMITTED, updatedSubmission.getId(),
                DateTimeFormatter.ISO_INSTANT.format(lastModified.atZone(ZoneId.of("UTC")))));
        return updatedSubmission;
    }

    @Override
    public SubmissionResponseApi updateSubmissionQueued(final Submission submission) {
        final var timestamp = timestampGenerator.generateTimestamp();
        final var updatedSubmission = Submission.builder(submission).withStatus(SubmissionStatus.PROCESSING).withLastModifiedAt(timestamp).build();
        updatedSubmission.getFormDetails()
            .getFileDetailsList()
            .forEach(fileDetails -> this.handleFile(fileDetails, timestamp));
        LOGGER.debug("Attempting to update submission status to PROCESSING for submission with id: [%s]".formatted(
            submission.getId()));
        submissionRepository.updateSubmission(updatedSubmission);
        LOGGER.debug("Updated submission status to PROCESSING for submission with id: [%s] at [%s]".formatted(
            submission.getId(),
            DateTimeFormatter.ISO_INSTANT.format(timestamp.atZone(ZoneId.of("UTC")))));
        return new SubmissionResponseApi(submission.getId());
    }

    private void handleFile(final FileDetails fileDetails, final LocalDateTime timestamp) {
        fileDetails.setConversionStatus(FileConversionStatus.QUEUED);
        fileDetails.setLastModifiedAt(timestamp);
    }

    @Override
    public SubmissionResponseApi updateSubmissionBarcode(final String id, final String barcode) {
        LOGGER.debug(String.format("Attempting to update barcode for submission with id: [%s]", id));
        submissionRepository.updateBarcode(id, barcode);
        LOGGER.debug("Updated barcode for submission with id: [%s]".formatted(id));
        return new SubmissionResponseApi(id);
    }

    @Override
    public SubmissionResponseApi updateSubmissionStatus(final String id, final SubmissionStatus status) {
        LOGGER.debug("Attempting to update status for submission with id: [%s]".formatted(id));
        submissionRepository.updateSubmissionStatus(id, status);
        LOGGER.debug("Updated status for submission with id: [%s]".formatted(id));
        return new SubmissionResponseApi(id);
    }

    @Override
    public SubmissionResponseApi updateSubmissionConfirmAuthorised(final String id, final Boolean confirmAuthorised) {
        LOGGER.debug("Attempting to update authorised for submission with id: [%s]".formatted(id));
        final var submission = this.getSubmissionWithCheckedStatus(id, UPDATABLE_STATUSES);
        final var updatedSubmission = Submission.builder(submission).withConfirmAuthorised(confirmAuthorised).build();
        submissionRepository.updateSubmission(updatedSubmission);
        LOGGER.debug("Successfully updated confirm authorised for submission with id: [%s]".formatted(id));
        return new SubmissionResponseApi(id);
    }

    @Override
    public void updateSubmission(final Submission submission) {
        LOGGER.debug(
            "Attempting to update submission with id: [%s]".formatted(submission.getId()));
        final var lastModified = timestampGenerator.generateTimestamp();
        final var updatedSubmission = Submission.builder(submission).withLastModifiedAt(lastModified).build();
        submissionRepository.updateSubmission(updatedSubmission);
        LOGGER.debug("Updated submission with id: [%s] at [%s]".formatted(submission.getId(),
            DateTimeFormatter.ISO_INSTANT.format(lastModified.atZone(ZoneId.of("UTC")))));
    }


    private Submission getSubmissionWithCheckedStatus(final String id,
        final Set<SubmissionStatus> acceptableStatusSet) {

        final var submission = submissionRepository.read(id);

        // check if submission exists and has the correct status
        final var debugMap = getDebugMap(id);

        if (submission == null) {
            LOGGER.errorContext(id, "Could not locate submission", null, debugMap);
            throw new SubmissionNotFoundException(
                "Could not locate submission with id: [%s]".formatted(id));
        } else if (!acceptableStatusSet.contains(submission.getStatus())) {
            LOGGER.errorContext(id, "Submission status wasn't in %s, couldn't update".formatted(
                acceptableStatusSet), null, debugMap);
            throw new SubmissionIncorrectStateException(
                "Submission status for [%s] wasn't in %s, couldn't update".formatted(id,
                    acceptableStatusSet));
        }

        return submission;
    }

    private String getPaymentCharge(final String formType) {
        final var formTemplate = formType != null ? formTemplateService.getFormTemplate(formType) : null;
        String result = null;
        final var now = LocalDateTime.now(clock);

        if (formTemplate != null) {
            final var paymentCharge = formTemplate.getPaymentCharge();

            if (StringUtils.isNotBlank(paymentCharge)) {

                LOGGER.debug("Payment fee at [%s] for form [%s] is [%s]".formatted(
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(now), formType, paymentCharge));

                final var template = paymentTemplateService.getPaymentTemplate(paymentCharge, now);

                result = template.map(t -> t.getItems().getFirst().getAmount()).orElse(null);
            }
        }
        if (result == null) {
            LOGGER.debug("Payment fee at [%s] for form [%s] is [N/A]".formatted(
                DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(now), formType));
        }

        return result;
    }


    private Map<String, Object> getDebugMap(final String id) {
        final Map<String, Object> debug = new HashMap<>();

        debug.put("submissionId", id);

        return debug;
    }
}
