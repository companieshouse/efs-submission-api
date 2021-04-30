package uk.gov.companieshouse.efs.api.submissions.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang.StringUtils;
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
import uk.gov.companieshouse.api.model.paymentsession.SessionListApi;
import uk.gov.companieshouse.efs.api.email.EmailService;
import uk.gov.companieshouse.efs.api.email.model.ExternalConfirmationEmailModel;
import uk.gov.companieshouse.efs.api.formtemplates.service.FormTemplateService;
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


    @Autowired
    public SubmissionServiceImpl(SubmissionRepository submissionRepository, SubmissionMapper submissionMapper,
        PresenterMapper presenterMapper, CompanyMapper companyMapper, FileDetailsMapper fileDetailsMapper,
        CurrentTimestampGenerator timestampGenerator,
        ConfirmationReferenceGeneratorService confirmationReferenceGenerator, FormTemplateService formTemplateService,
        PaymentTemplateService paymentTemplateService, EmailService emailService, Validator<Submission> validator) {
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
        Submission submission = this.getSubmissionForUpdate(id);
        submission.setCompany(companyMapper.map(companyApi));
        submissionRepository.updateSubmission(submission);
        LOGGER.debug(String.format("Successfully updated company details for submission with id: [%s]", id));
        return new SubmissionResponseApi(id);
    }

    @Override
    public SubmissionResponseApi updateSubmissionWithForm(String id, FormTypeApi formApi) {
        LOGGER.debug(String.format("Attempting to update form type for submission with id: [%s]", id));
        Submission submission = this.getSubmissionForUpdate(id);
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
        Submission submission = this.getSubmissionForUpdate(id);
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
        LOGGER.debug(String.format("Attempting to update payment sessions for submission with id: [%s]", id));
        Submission submission = this.getSubmissionForUpdate(id);

        submission.setPaymentSessions(paymentSessions);
        submissionRepository.updateSubmission(submission);
        LOGGER.debug(String.format("Successfully updated payment sessions for submission with id: [%s]", id));

        return new SubmissionResponseApi(id);
    }

    @Override
    public SubmissionResponseApi completeSubmission(String id) throws SubmissionValidationException {

        LOGGER.debug(String.format("Attempting to complete submission for id: [%s]", id));

        Submission submission = this.getSubmissionForUpdate(id);

        // check submission mandatory fields (validator)
        try {
            validator.validate(submission);
        } catch (SubmissionValidationException ve) {
            LOGGER.info(ve.getMessage());
            Map<String, Object> debug = new HashMap<>();
            debug.put("submissionId", id);
            debug.put("exceptionMessage", ve.getMessage());
            LOGGER.errorContext(id, "Submission invalid ", null, debug);
            throw ve;
        }

        // TODO: 
        // Assumes there is PATCH request handling (controller) that does this:
        //  1. update corresponding payment session status to "paid"/"failed"
        //  2. update submission status to SUBMITTED/PAYMENT_FAILED respectively
        //  3. if PAYMENT_FAILED, send payment user email

        emailService.sendExternalConfirmation(new ExternalConfirmationEmailModel(submission));

        calculateSubmissionStatus(submission);
        updateSubmission(submission);

        LOGGER.debug(String.format("Successfully completed submission for id: [%s]", id));

        return new SubmissionResponseApi(id);
    }

    private void calculateSubmissionStatus(final Submission submission) {
        //  if fee_on_submission exists
        //      if submission.status == OPEN
        //          (submission.status => PAYMENT_REQUIRED)
        //      else
        //          (don't update submission.status)
        //     else
        //          (submission.status => SUBMITTED)
        if (submission.getFeeOnSubmission() != null) {
            if (submission.getStatus() == SubmissionStatus.OPEN) {
                submission.setStatus(SubmissionStatus.PAYMENT_REQUIRED);
            }
        } else {
            submission.setStatus(SubmissionStatus.SUBMITTED);
            submission.setSubmittedAt(timestampGenerator.generateTimestamp());
        }
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
        Submission submission = this.getSubmissionForUpdate(id);
        submission.setConfirmAuthorised(confirmAuthorised);
        submissionRepository.updateSubmission(submission);
        LOGGER.debug(String.format("Successfully updated confirm authorised for submission with id: [%s]", id));
        return new SubmissionResponseApi(id);
    }

    @Override
    public void updateSubmission(Submission submission) {
        LOGGER.debug(String.format("Attempting to update submission with id: [%s]", submission.getId()));
        submission.setLastModifiedAt(timestampGenerator.generateTimestamp());
        submissionRepository.updateSubmission(submission);
        LOGGER.debug(String.format("Updated submission with id: [%s]", submission.getId()));
    }


    private Submission getSubmissionForUpdate(String id) {

        Submission submission = submissionRepository.read(id);

        // check if submission exists and has the correct status
        Map<String, Object> debug = new HashMap<>();
        debug.put("submissionId", id);
        if (submission == null) {
            LOGGER.errorContext(id, "Could not locate submission", null, debug);
            throw new SubmissionNotFoundException(String.format("Could not locate submission with id: [%s]", id));
        } else if (submission.getStatus() != SubmissionStatus.OPEN) {
            LOGGER.errorContext(id, "Submission status wasn't OPEN, couldn't update", null, debug);
            throw new SubmissionIncorrectStateException(
                String.format("Submission status for [%s] wasn't OPEN, couldn't update", id));
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

}
