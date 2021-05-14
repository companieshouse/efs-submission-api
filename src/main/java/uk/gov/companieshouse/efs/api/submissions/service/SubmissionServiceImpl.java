package uk.gov.companieshouse.efs.api.submissions.service;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateApi;
import uk.gov.companieshouse.api.model.efs.submissions.CompanyApi;
import uk.gov.companieshouse.api.model.efs.submissions.FileConversionStatus;
import uk.gov.companieshouse.api.model.efs.submissions.FileListApi;
import uk.gov.companieshouse.api.model.efs.submissions.FormTypeApi;
import uk.gov.companieshouse.api.model.efs.submissions.PresenterApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionResponseApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus;
import uk.gov.companieshouse.api.model.paymentsession.SessionApi;
import uk.gov.companieshouse.api.model.paymentsession.SessionListApi;
import uk.gov.companieshouse.efs.api.email.EmailService;
import uk.gov.companieshouse.efs.api.email.model.ExternalNotificationEmailModel;
import uk.gov.companieshouse.efs.api.formtemplates.service.FormTemplateService;
import uk.gov.companieshouse.efs.api.payment.PaymentClose;
import uk.gov.companieshouse.efs.api.payment.entity.PaymentTemplate;
import uk.gov.companieshouse.efs.api.payment.service.PaymentTemplateService;
import uk.gov.companieshouse.efs.api.submissions.mapper.CompanyMapper;
import uk.gov.companieshouse.efs.api.submissions.mapper.FileDetailsMapper;
import uk.gov.companieshouse.efs.api.submissions.mapper.PresenterMapper;
import uk.gov.companieshouse.efs.api.submissions.mapper.SubmissionMapper;
import uk.gov.companieshouse.efs.api.submissions.model.FileDetails;
import uk.gov.companieshouse.efs.api.submissions.model.FormDetails;
import uk.gov.companieshouse.efs.api.submissions.model.Presenter;
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
        "Updated submission status to %s for submission with id: [%s]";

    private SubmissionRepository submissionRepository;
    private SubmissionMapper submissionMapper;
    private PresenterMapper presenterMapper;
    private CompanyMapper companyMapper;
    private FileDetailsMapper fileDetailsMapper;
    private CurrentTimestampGenerator timestampGenerator;
    private ConfirmationReferenceGeneratorService confirmationReferenceGenerator;
    private FormTemplateService formTemplateService;
    private PaymentTemplateService paymentTemplateService;
    private EmailService emailService;
    private Validator<Submission> validator;
    public static final ImmutableSet<SubmissionStatus> UPDATABLE_STATUSES =
        Sets.immutableEnumSet(SubmissionStatus.OPEN, SubmissionStatus.PAYMENT_REQUIRED,
            SubmissionStatus.PAYMENT_RECEIVED, SubmissionStatus.PAYMENT_FAILED);

    @Autowired
    public SubmissionServiceImpl(SubmissionRepository submissionRepository,
        SubmissionMapper submissionMapper, PresenterMapper presenterMapper,
        CompanyMapper companyMapper, FileDetailsMapper fileDetailsMapper,
        CurrentTimestampGenerator timestampGenerator,
        ConfirmationReferenceGeneratorService confirmationReferenceGenerator,
        FormTemplateService formTemplateService, PaymentTemplateService paymentTemplateService,
        EmailService emailService, Validator<Submission> validator) {
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
    }

    @Override
    public SubmissionApi readSubmission(String id) {
        Submission submission = submissionRepository.read(id);
        if (submission != null) {
            return submissionMapper.map(submission);
        } else {
            return null;
        }
    }

    @Override
    public SubmissionResponseApi createSubmission(PresenterApi presenterApi) {
        LOGGER.debug(String.format("Attempting to create a submission with email: [%s]", presenterApi.getEmail()));
        Presenter presenter = presenterMapper.map(presenterApi);
        LocalDateTime timestamp = timestampGenerator.generateTimestamp();
        String confirmRef = confirmationReferenceGenerator.generateId();
        Submission submission = Submission.builder().withConfirmationReference(confirmRef).withPresenter(presenter)
                .withStatus(SubmissionStatus.OPEN).withCreatedAt(timestamp).withLastModifiedAt(timestamp).build();
        submissionRepository.create(submission);
        LOGGER.debug(String.format("Successfully created a submission with email: [%s] and id: [%s]", presenterApi.getEmail(), submission.getId()));
        return new SubmissionResponseApi(submission.getId());
    }

    @Override
    public SubmissionResponseApi updateSubmissionWithCompany(String id, CompanyApi companyApi) {
        LOGGER.debug(String.format("Attempting to update company details for submission with id: [%s]", id));
        Submission submission = this.getSubmissionWithCheckedStatus(id, UPDATABLE_STATUSES);
        submission.setCompany(companyMapper.map(companyApi));
        submissionRepository.updateSubmission(submission);
        LOGGER.debug(String.format("Successfully updated company details for submission with id: [%s]", id));
        return new SubmissionResponseApi(id);
    }

    @Override
    public SubmissionResponseApi updateSubmissionWithForm(String id, FormTypeApi formApi) {
        LOGGER.debug(String.format("Attempting to update form type for submission with id: [%s]", id));
        Submission submission = this.getSubmissionWithCheckedStatus(id, UPDATABLE_STATUSES);
        FormDetails formDetails = submission.getFormDetails();
        final String formType = formApi.getFormType();
        if (formDetails == null) {
            formDetails = FormDetails.builder().withFormType(formType).build();
        } else {
            formDetails.setFormType(formType);
        }
        LOGGER.debug(String.format("Attempting to update fee for submission with id: [%s]", id));
        submission.setFeeOnSubmission(getPaymentCharge(formType));
        submission.setFormDetails(formDetails);
        submissionRepository.updateSubmission(submission);
        LOGGER.debug(String.format("Successfully updated form type for submission with id: [%s]", id));
        return new SubmissionResponseApi(id);
    }

    @Override
    public SubmissionResponseApi updateSubmissionWithFileDetails(String id, FileListApi fileListApi) {
        LOGGER.debug(String.format("Attempting to update file details for submission with id: [%s]", id));
        Submission submission = this.getSubmissionWithCheckedStatus(id, UPDATABLE_STATUSES);
        FormDetails formDetails = submission.getFormDetails();
        if (formDetails == null) {
            formDetails = FormDetails.builder().withFileDetailsList(fileDetailsMapper.map(fileListApi)).build();
        } else {
            formDetails.setFileDetailsList(fileDetailsMapper.map(fileListApi));
        }
        submission.setFormDetails(formDetails);
        submissionRepository.updateSubmission(submission);
        LOGGER.debug(String.format("Successfully updated file details for submission with id: [%s]", id));

        return new SubmissionResponseApi(id);
    }

    @Override
    public SubmissionResponseApi updateSubmissionWithPaymentSessions(String id,
        SessionListApi paymentSessions) {
        LOGGER.debug(
            String.format("Attempting to update payment sessions for submission with id: [%s]",
                id));
        Submission submission = this.getSubmissionWithCheckedStatus(id, UPDATABLE_STATUSES);

        submission.setPaymentSessions(paymentSessions);
        submissionRepository.updateSubmission(submission);

        return new SubmissionResponseApi(id);
    }

    @Override
    public SubmissionResponseApi updateSubmissionWithPaymentOutcome(final String id,
        final PaymentClose paymentClose) {
        final Submission submission = getSubmissionWithCheckedStatus(id, UPDATABLE_STATUSES);
        final SubmissionStatus status = submission.getStatus();
        SubmissionStatus resultStatus;

        setPaymentSessionStatus(submission, paymentClose);
        LOGGER.debug(String.format("Updating submission status %s for submission with id: [%s]",
            status, submission.getId()));
        switch (status) {
            case OPEN:
                resultStatus = paymentClose.isFailed()
                    ? SubmissionStatus.OPEN
                    : SubmissionStatus.PAYMENT_RECEIVED;
                break;
            case PAYMENT_REQUIRED:
                resultStatus = paymentClose.isFailed()
                    ? SubmissionStatus.PAYMENT_FAILED
                    : SubmissionStatus.SUBMITTED;
                break;
            default:
                return new SubmissionResponseApi(id);
        }
        if (resultStatus == SubmissionStatus.SUBMITTED) {
            submission.setSubmittedAt(timestampGenerator.generateTimestamp());
        }
        submission.setStatus(resultStatus);
        submission.setLastModifiedAt(timestampGenerator.generateTimestamp());
        submissionRepository.updateSubmission(submission);
        LOGGER.debug(String.format(SUBMISSION_STATUS_MSG, resultStatus, submission.getId()));

        return new SubmissionResponseApi(id);
    }

    private void setPaymentSessionStatus(final Submission submission,
        final PaymentClose paymentClose) {
        LOGGER.debug(String.format(
            "Attempting to update payment session outcome for submission with id: [%s]",
            submission.getId()));

        final Optional<SessionApi> matchedSession =
            Optional.ofNullable(submission.getPaymentSessions())
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .filter(
                    s -> StringUtils.equals(s.getSessionId(), paymentClose.getPaymentReference()))
                .findFirst();
        matchedSession.orElseThrow(
            () -> new SubmissionIncorrectStateException("payment reference not matched"))
            .setSessionStatus(paymentClose.getStatus());
    }

    @Override
    public SubmissionResponseApi completeSubmission(String id)
        throws SubmissionValidationException {

        LOGGER.debug(String.format("Attempting to complete submission for id: [%s]", id));

        Submission submission = this.getSubmissionWithCheckedStatus(id, UPDATABLE_STATUSES);

        // check submission mandatory fields (validator)
        try {
            validator.validate(submission);
        } catch (SubmissionValidationException ve) {
            LOGGER.info(ve.getMessage());

            final Map<String, Object> debug = getDebugMap(id);

            debug.put("exceptionMessage", ve.getMessage());
            LOGGER.errorContext(id, "Submission invalid ", null, debug);
            throw ve;
        }

        progressSubmissionStatus(submission);
        updateSubmission(submission);
        LOGGER.debug(String.format("Successfully completed submission for id: [%s]", id));

        final SubmissionStatus status = submission.getStatus();

        if (submission.getFeeOnSubmission() == null || status == SubmissionStatus.SUBMITTED) {
            emailService.sendExternalConfirmation(new ExternalNotificationEmailModel(submission));
        }
        if (status == SubmissionStatus.PAYMENT_FAILED) {
            emailService.sendExternalPaymentFailedNotification(
                new ExternalNotificationEmailModel(submission));
        }

        return new SubmissionResponseApi(id);
    }

    private void progressSubmissionStatus(final Submission submission) {
        final SessionListApi paymentSessions = submission.getPaymentSessions();
        final SubmissionStatus status = submission.getStatus();

        LOGGER.debug(String.format("Updating submission status %s for submission with id: [%s]",
            status, submission.getId()));
        switch (status) {
            case OPEN:
                if (submission.getFeeOnSubmission() == null || anySessionWithStatus(paymentSessions,
                    PaymentTemplate.Status.PAID)) {
                    setAsSubmitted(submission);
                } else if (anySessionWithStatus(paymentSessions, PaymentTemplate.Status.PENDING)) {
                    submission.setStatus(SubmissionStatus.PAYMENT_REQUIRED);
                    LOGGER.debug(String.format(SUBMISSION_STATUS_MSG, status,
                        submission.getId()));
                }
                break;
            case PAYMENT_RECEIVED:
                if (anySessionWithStatus(paymentSessions, PaymentTemplate.Status.PAID)) {
                    setAsSubmitted(submission);
                }
                break;
            default:
                // intentionally empty
                break;
        }
    }

    private void setAsSubmitted(final Submission submission) {
        submission.setStatus(SubmissionStatus.SUBMITTED);
        submission.setSubmittedAt(timestampGenerator.generateTimestamp());
        LOGGER.debug(
            String.format(SUBMISSION_STATUS_MSG, SubmissionStatus.SUBMITTED, submission.getId()));
    }

    private boolean anySessionWithStatus(final SessionListApi paymentSessions,
        final PaymentTemplate.Status status) {
        return Optional.ofNullable(paymentSessions)
            .orElseGet(SessionListApi::new)
            .stream()
            .anyMatch(s -> StringUtils.equals(status.toString(), s.getSessionStatus()));
    }

    @Override
    public SubmissionResponseApi updateSubmissionQueued(Submission submission) {
        submission.setStatus(SubmissionStatus.PROCESSING);
        LocalDateTime timestamp = timestampGenerator.generateTimestamp();
        submission.setLastModifiedAt(timestamp);
        submission.getFormDetails()
            .getFileDetailsList()
            .forEach(fileDetails -> this.handleFile(fileDetails, timestamp));
        LOGGER.debug(String.format(
            "Attempting to update submission status to PROCESSING for submission with id: [%s]",
            submission.getId()));
        submissionRepository.updateSubmission(submission);
        LOGGER.debug(String.format("Updated submission status to PROCESSING for submission with id: [%s]", submission.getId()));
        return new SubmissionResponseApi(submission.getId());
    }

    private void handleFile(FileDetails fileDetails, LocalDateTime timestamp) {
        fileDetails.setConversionStatus(FileConversionStatus.QUEUED);
        fileDetails.setLastModifiedAt(timestamp);
    }

    @Override
    public SubmissionResponseApi updateSubmissionBarcode(String id, String barcode) {
        LOGGER.debug(String.format("Attempting to update barcode for submission with id: [%s]", id));
        submissionRepository.updateBarcode(id, barcode);
        LOGGER.debug(String.format("Updated barcode for submission with id: [%s]", id));
        return new SubmissionResponseApi(id);
    }

    @Override
    public SubmissionResponseApi updateSubmissionStatus(String id, SubmissionStatus status) {
        LOGGER.debug(String.format("Attempting to update status for submission with id: [%s]", id));
        submissionRepository.updateSubmissionStatus(id, status);
        LOGGER.debug(String.format("Updated status for submission with id: [%s]", id));
        return new SubmissionResponseApi(id);
    }

    @Override
    public SubmissionResponseApi updateSubmissionConfirmAuthorised(final String id, final Boolean confirmAuthorised) {
        LOGGER.debug(String.format("Attempting to update authorised for submission with id: [%s]", id));
        Submission submission = this.getSubmissionWithCheckedStatus(id, UPDATABLE_STATUSES);
        submission.setConfirmAuthorised(confirmAuthorised);
        submissionRepository.updateSubmission(submission);
        LOGGER.debug(String.format("Successfully updated confirm authorised for submission with id: [%s]", id));
        return new SubmissionResponseApi(id);
    }

    @Override
    public void updateSubmission(Submission submission) {
        LOGGER.debug(
            String.format("Attempting to update submission with id: [%s]", submission.getId()));
        submission.setLastModifiedAt(timestampGenerator.generateTimestamp());
        submissionRepository.updateSubmission(submission);
        LOGGER.debug(String.format("Updated submission with id: [%s]", submission.getId()));
    }


    private Submission getSubmissionWithCheckedStatus(String id,
        final ImmutableSet<SubmissionStatus> acceptableStatusSet) {

        Submission submission = submissionRepository.read(id);

        // check if submission exists and has the correct status
        final Map<String, Object> debugMap = getDebugMap(id);

        if (submission == null) {
            LOGGER.errorContext(id, "Could not locate submission", null, debugMap);
            throw new SubmissionNotFoundException(
                String.format("Could not locate submission with id: [%s]", id));
        } else if (!acceptableStatusSet.contains(submission.getStatus())) {
            LOGGER.errorContext(id, String.format("Submission status wasn't in %s, couldn't update",
                acceptableStatusSet), null, debugMap);
            throw new SubmissionIncorrectStateException(
                String.format("Submission status for [%s] wasn't in %s, couldn't update", id,
                    acceptableStatusSet));
        }

        return submission;
    }

    private String getPaymentCharge(final String formType) {
        final FormTemplateApi formTemplate = formType != null ? formTemplateService.getFormTemplate(formType) : null;
        String result = null;

        if (formTemplate != null) {
            final String paymentCharge = formTemplate.getPaymentCharge();

            if (StringUtils.isNotBlank(paymentCharge)) {
                LOGGER.debug(String.format("Payment charge for form [%s] is [%s]", formType, paymentCharge));

                final Optional<PaymentTemplate> template = paymentTemplateService.getTemplate(paymentCharge);

                result = template.map(t -> t.getItems().get(0).getAmount()).orElse(null);
            }
        }
        if (result == null) {
            LOGGER.debug("Payment charge for form is [N/A]");
        }

        return result;
    }


    private Map<String, Object> getDebugMap(final String id) {
        final Map<String, Object> debug = new HashMap<>();

        debug.put("submissionId", id);

        return debug;
    }
}
