package uk.gov.companieshouse.efs.api.events.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.efs.events.FileConversionStatusApi;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateApi;
import uk.gov.companieshouse.api.model.efs.submissions.FileConversionStatus;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus;
import uk.gov.companieshouse.efs.api.email.EmailService;
import uk.gov.companieshouse.efs.api.email.exception.EmailServiceException;
import uk.gov.companieshouse.efs.api.email.model.InternalFailedConversionModel;
import uk.gov.companieshouse.efs.api.events.service.exception.BarcodeException;
import uk.gov.companieshouse.efs.api.events.service.exception.FesLoaderException;
import uk.gov.companieshouse.efs.api.events.service.exception.InvalidTiffException;
import uk.gov.companieshouse.efs.api.events.service.exception.TiffDownloadException;
import uk.gov.companieshouse.efs.api.events.service.model.FesFileModel;
import uk.gov.companieshouse.efs.api.events.service.model.FesLoaderModel;
import uk.gov.companieshouse.efs.api.formtemplates.service.FormTemplateService;
import uk.gov.companieshouse.efs.api.submissions.model.FileDetails;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.submissions.repository.SubmissionRepository;
import uk.gov.companieshouse.efs.api.submissions.service.SubmissionService;
import uk.gov.companieshouse.efs.api.submissions.service.exception.FileIncorrectStateException;
import uk.gov.companieshouse.efs.api.submissions.service.exception.FileNotFoundException;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionIncorrectStateException;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionNotFoundException;
import uk.gov.companieshouse.efs.api.util.CurrentTimestampGenerator;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Service
public class EventServiceImpl implements EventService {

    private static final Logger LOGGER = LoggerFactory.getLogger("efs-submission-api");
    private static final String SUBMISSION_NOT_FOUND_MESSAGE =
        "Could not locate submission with id: [%s]";
    private static final String FILE_NOT_FOUND_MESSAGE =
        "Could not locate file with id [%s] on submission with id: [%s]";
    private static final String SUBMISSION_INCORRECT_STATE_MESSAGE =
        "Submission status for [%s] wasn't [%s], couldn't update";
    private static final String FILE_INCORRECT_STATE_MESSAGE =
        "Status for file with id [%s] wasn't [%s] on submission with id [%s], couldn't update";

    private SubmissionService submissionService;
    private FormTemplateService formTemplateService;
    private EmailService emailService;
    private SubmissionRepository repository;
    private CurrentTimestampGenerator currentTimestampGenerator;
    private int maxQueuedMessages;
    private DecisionEngine decisionEngine;
    private BarcodeGeneratorService barcodeGeneratorService;
    private TiffDownloadService tiffDownloadService;
    private FesLoaderService fesLoaderService;
    private ExecutionEngine executionEngine;
    private DelayedSubmissionHandlerContext delayedSubmissionHandlerContext;

    @Autowired
    public EventServiceImpl(SubmissionService submissionService,
        final FormTemplateService formTemplateService, EmailService emailService,
        SubmissionRepository repository, CurrentTimestampGenerator currentTimestampGenerator,
        @Value("${max.queue.messages}") int maxQueuedMessages, DecisionEngine decisionEngine,
        BarcodeGeneratorService barcodeGeneratorService, TiffDownloadService tiffDownloadService,
        FesLoaderService fesLoaderService, ExecutionEngine executionEngine, DelayedSubmissionHandlerContext delayedSubmissionHandlerContext) {
        this.submissionService = submissionService;
        this.formTemplateService = formTemplateService;
        this.emailService = emailService;
        this.repository = repository;
        this.currentTimestampGenerator = currentTimestampGenerator;
        this.maxQueuedMessages = maxQueuedMessages;
        this.decisionEngine = decisionEngine;
        this.barcodeGeneratorService = barcodeGeneratorService;
        this.tiffDownloadService = tiffDownloadService;
        this.fesLoaderService = fesLoaderService;
        this.executionEngine = executionEngine;
        this.delayedSubmissionHandlerContext = delayedSubmissionHandlerContext;
    }

    @Override
    public List<Submission> findSubmissionsByStatus(SubmissionStatus status) {
        return repository.findByStatusOrderByPriority(status, maxQueuedMessages);
    }

    @Override
    public void processFiles() {
        executionEngine.execute(decisionEngine.evaluateSubmissions(this.findSubmissionsByStatus(SubmissionStatus.SUBMITTED)));
    }

    @Override
    public void updateConversionFileStatus(String submissionId, String fileId, FileConversionStatusApi fileConversionStatus) {

        Submission submission = getSubmission(submissionId, fileId);
        LocalDateTime now = currentTimestampGenerator.generateTimestamp();

        submission.getFormDetails().getFileDetailsList().stream()
                .filter(file -> fileId.equals(file.getFileId()))
                .forEach(file -> {
                    file.setConversionStatus(FileConversionStatus.valueOf(fileConversionStatus.getConversionStatus().toString()));
                    file.setConvertedFileId(fileConversionStatus.getConvertedFileId());
                    file.setNumberOfPages(fileConversionStatus.getNumberOfPages());
                    file.setLastModifiedAt(now);
                });

        Submission updatedSubmission = Submission.builder(submission).withLastModifiedAt(now).build();

        // have all files been converted or failed
        if (areAllFilesConvertedOrFailed(submission.getFormDetails().getFileDetailsList())) {

            boolean allConverted = submission.getFormDetails().getFileDetailsList().stream()
                    .allMatch(file -> file.getConversionStatus().equals(FileConversionStatus.CONVERTED));

            if (allConverted) {
                // set status to be READY_TO_SUBMIT
                updatedSubmission = Submission.builder(submission).withStatus(SubmissionStatus.READY_TO_SUBMIT).build();
            } else {
                // set status to be REJECTED_BY_DOCUMENT_CONVERTER
                updatedSubmission = Submission.builder(submission).withStatus(SubmissionStatus.REJECTED_BY_DOCUMENT_CONVERTER).build();

                try {

                    // send an internal failed document conversion email
                    InternalFailedConversionModel internalFailedConversionModel =
                            new InternalFailedConversionModel(submission,
                                    submission.getFormDetails().getFileDetailsList().stream()
                                            .filter(fileDetail -> fileDetail.getConversionStatus() == FileConversionStatus.FAILED)
                                            .map(FileDetails::getFileName).collect(Collectors.toList()));
                    emailService.sendInternalFailedConversion(internalFailedConversionModel);
                } catch (EmailServiceException ex) {
                    LOGGER.errorContext(submissionId,
                            "Failed to send failed conversion email for submission",
                            null, null);
                }
            }
        }

        LOGGER.debug(String.format("Updating file [%s] in submission [%s] "
                                   + "with converted file id [%s], "
                                   + "conversion status [%s] "
                                   + "and submission status [%s]", fileId, submissionId,
            fileConversionStatus.getConvertedFileId(), fileConversionStatus.getConversionStatus(),
            submission.getStatus()));

        repository.updateSubmission(updatedSubmission);
    }

    @Override
    public void submitToFes() {
        List<Submission> submissions = findSubmissionsByStatus(SubmissionStatus.READY_TO_SUBMIT);
        submissions.forEach(submission -> {
            try {
                // generate barcode
                String barcode = submission.getFormDetails().getBarcode();

                LocalDateTime submittedAt = submission.getSubmittedAt() == null ? submission.getCreatedAt() : submission.getSubmittedAt();

                if (barcode == null) {
                    barcode = barcodeGeneratorService.getBarcode(submittedAt);
                    LOGGER.debug(
                            String.format("Generated barcode for submission [%s]: %s", submission.getId(), barcode));
                    submissionService.updateSubmissionBarcode(submission.getId(), barcode);
                }

                final String efsFormId = submission.getFormDetails().getFormType();
                final FormTemplateApi formTemplate = formTemplateService.getFormTemplate(efsFormId);
                
                if (formTemplate == null) {
                    throw new SubmissionIncorrectStateException(
                        String.format("Unrecognised form type '%s' in form details", efsFormId));
                }
                
                final String fesDocType =
                    Optional.ofNullable(formTemplate.getFesDocType()).orElseGet(formTemplate::getFormType);
                LOGGER.debug(String.format("Submit to FES: [%s]", fesDocType));

                // retrieve TIFF file
                List<FesFileModel> tiffFiles = new ArrayList<>();
                submission.getFormDetails().getFileDetailsList().forEach(
                        file -> tiffFiles.add(
                                new FesFileModel(
                                        tiffDownloadService.downloadTiffFile(file.getConvertedFileId()), file.getNumberOfPages())));
                LOGGER.debug(String.format("Retrieved [%d] files for submission [%s] from S3", tiffFiles.size(), submission.getId()));

                // insert into FES DB

                fesLoaderService.insertSubmission(
                    new FesLoaderModel(barcode, submission.getCompany().getCompanyName(),
                        submission.getCompany().getCompanyNumber(), fesDocType,
                        formTemplate.isSameDay(), tiffFiles, submittedAt));
                LOGGER.debug(String.format(
                    "Inserted submission details into FES DB for submission [%s], form [%s], same-day [%s]",
                    submission.getId(), fesDocType, formTemplate.isSameDay() ? "Y" : "N"));

                submissionService.updateSubmissionStatus(submission.getId(),
                    SubmissionStatus.SENT_TO_FES);

            }
            catch (SubmissionIncorrectStateException | BarcodeException | TiffDownloadException | FesLoaderException | InvalidTiffException ex) {
                LOGGER.errorContext(submission.getId(), "Unable to submit to fes" + ex.getMessage(),
                    ex, null);
            }
        });
    }

    @Override
    public void handleDelayedSubmissions(final DelayedSubmissionHandlerContext.ServiceLevel serviceLevel) {
        final LocalDateTime handledAt = currentTimestampGenerator.generateTimestamp();
        final DelayedSubmissionHandlerStrategy strategy =
            delayedSubmissionHandlerContext.getStrategy(serviceLevel);
        final List<Submission> delayedSubmissions = strategy.findDelayedSubmissions(handledAt);

        strategy.buildAndSendEmails(delayedSubmissions, handledAt);
    }

    private boolean areAllFilesConvertedOrFailed(List<FileDetails> fileDetails) {
        return fileDetails.stream().allMatch(
            file -> file.getConversionStatus().equals(FileConversionStatus.CONVERTED) || file
                .getConversionStatus().equals(FileConversionStatus.FAILED));
    }

    private Submission getSubmission(String submissionId, String fileId) {
        // get the submission
        Submission submission = repository.read(submissionId);

        Map<String, Object> debug = new HashMap<>();
        debug.put("submissionId", submissionId);
        debug.put("fileId", fileId);
        if (submission == null) {
            LOGGER.errorContext(submissionId,
                    String.format(SUBMISSION_NOT_FOUND_MESSAGE, submissionId), null, debug);
            throw new SubmissionNotFoundException(String.format(SUBMISSION_NOT_FOUND_MESSAGE, submissionId));
        } else if (submission.getStatus() != SubmissionStatus.PROCESSING) {
            LOGGER.errorContext(submissionId,
                    String.format(SUBMISSION_INCORRECT_STATE_MESSAGE, submissionId,
                            SubmissionStatus.PROCESSING), null, debug);
            throw new SubmissionIncorrectStateException(
                    String.format(SUBMISSION_INCORRECT_STATE_MESSAGE, submissionId, SubmissionStatus.PROCESSING));
        }

        Optional<FileDetails> fileDetailsOpt = submission.getFormDetails().getFileDetailsList().stream()
                .filter(file -> fileId.equals(file.getFileId())).findFirst();

        if (fileDetailsOpt.isPresent()) {
            if (fileDetailsOpt.get().getConversionStatus() != FileConversionStatus.QUEUED) {
                LOGGER.errorContext(submissionId, String.format(FILE_INCORRECT_STATE_MESSAGE, fileId,
                        FileConversionStatus.QUEUED, submissionId), null, debug);
                throw new FileIncorrectStateException(
                        String.format(FILE_INCORRECT_STATE_MESSAGE, fileId, FileConversionStatus.QUEUED, submissionId));
            }
        } else {
            LOGGER.errorContext(submissionId, String.format(FILE_NOT_FOUND_MESSAGE, fileId,
                    submissionId), null, debug);
            throw new FileNotFoundException(String.format(FILE_NOT_FOUND_MESSAGE, fileId, submissionId));
        }
        return submission;
    }
}
