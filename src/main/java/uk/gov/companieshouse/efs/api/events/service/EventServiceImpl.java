package uk.gov.companieshouse.efs.api.events.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import uk.gov.companieshouse.api.model.efs.submissions.FileConversionStatus;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus;
import uk.gov.companieshouse.efs.api.email.EmailService;
import uk.gov.companieshouse.efs.api.email.FormCategoryToEmailAddressService;
import uk.gov.companieshouse.efs.api.email.exception.EmailServiceException;
import uk.gov.companieshouse.efs.api.email.model.DelayedSubmissionBusinessEmailModel;
import uk.gov.companieshouse.efs.api.email.model.DelayedSubmissionBusinessModel;
import uk.gov.companieshouse.efs.api.email.model.DelayedSubmissionSupportEmailModel;
import uk.gov.companieshouse.efs.api.email.model.DelayedSubmissionSupportModel;
import uk.gov.companieshouse.efs.api.email.model.InternalFailedConversionModel;
import uk.gov.companieshouse.efs.api.events.service.exception.BarcodeException;
import uk.gov.companieshouse.efs.api.events.service.exception.FesLoaderException;
import uk.gov.companieshouse.efs.api.events.service.exception.InvalidTiffException;
import uk.gov.companieshouse.efs.api.events.service.exception.TiffDownloadException;
import uk.gov.companieshouse.efs.api.events.service.model.FesFileModel;
import uk.gov.companieshouse.efs.api.events.service.model.FesLoaderModel;
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
    private static final String SUBMISSION_NOT_FOUND_MESSAGE = "Could not locate submission with id: [%s]";
    private static final String FILE_NOT_FOUND_MESSAGE = "Could not locate file with id [%s] on submission with id: [%s]";
    private static final String SUBMISSION_INCORRECT_STATE_MESSAGE = "Submission status for [%s] wasn't [%s], couldn't update";
    private static final String FILE_INCORRECT_STATE_MESSAGE = "Status for file with id [%s] wasn't [%s] on submission with id [%s], couldn't update";
    private static final String SUBMITTED_AT_BUSINESS_EMAIL_DATE_FORMAT = "dd MMMM yyyy";
    private static final String SUBMITTED_AT_SUPPORT_EMAIL_DATE_FORMAT = "dd/MM/yyyy";

    private SubmissionService submissionService;
    private EmailService emailService;
    private SubmissionRepository repository;
    private CurrentTimestampGenerator currentTimestampGenerator;
    private int maxQueuedMessages;
    private DecisionEngine decisionEngine;
    private BarcodeGeneratorService barcodeGeneratorService;
    private TiffDownloadService tiffDownloadService;
    private FesLoaderService fesLoaderService;
    private ExecutionEngine executionEngine;
    private FormCategoryToEmailAddressService formCategoryToEmailAddressService;
    private int supportDelayInHours;
    private int businessDelayInHours;

    @Autowired
    public EventServiceImpl(SubmissionService submissionService, EmailService emailService,
                            SubmissionRepository repository, CurrentTimestampGenerator currentTimestampGenerator,
                            @Value("${max.queue.messages}") int maxQueuedMessages, DecisionEngine decisionEngine,
                            BarcodeGeneratorService barcodeGeneratorService, TiffDownloadService tiffDownloadService,
                            FesLoaderService fesLoaderService, ExecutionEngine executionEngine, FormCategoryToEmailAddressService formCategoryToEmailAddressService,
                            @Value("${submission.processing.support.hours}") int supportDelayInHours,
                            @Value("${submission.processing.business.hours}") int businessDelayInHours) {
        this.submissionService = submissionService;
        this.emailService = emailService;
        this.repository = repository;
        this.currentTimestampGenerator = currentTimestampGenerator;
        this.maxQueuedMessages = maxQueuedMessages;
        this.decisionEngine = decisionEngine;
        this.barcodeGeneratorService = barcodeGeneratorService;
        this.tiffDownloadService = tiffDownloadService;
        this.fesLoaderService = fesLoaderService;
        this.executionEngine = executionEngine;
        this.formCategoryToEmailAddressService = formCategoryToEmailAddressService;
        this.supportDelayInHours = supportDelayInHours;
        this.businessDelayInHours = businessDelayInHours;
    }

    @Override
    public List<Submission> findSubmissionsByStatus(SubmissionStatus status) {
        return repository.findByStatus(status, maxQueuedMessages);
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

        submission.setLastModifiedAt(now);

        // have all files been converted or failed
        if (areAllFilesConvertedOrFailed(submission.getFormDetails().getFileDetailsList())) {

            boolean allConverted = submission.getFormDetails().getFileDetailsList().stream()
                    .allMatch(file -> file.getConversionStatus().equals(FileConversionStatus.CONVERTED));

            if (allConverted) {
                // set status to be READY_TO_SUBMIT
                submission.setStatus(SubmissionStatus.READY_TO_SUBMIT);
            } else {
                // set status to be REJECTED_BY_DOCUMENT_CONVERTER
                submission.setStatus(SubmissionStatus.REJECTED_BY_DOCUMENT_CONVERTER);

                try {

                    // send an internal failed document conversion email
                    InternalFailedConversionModel internalFailedConversionModel =
                            new InternalFailedConversionModel(submission,
                                    submission.getFormDetails().getFileDetailsList().stream()
                                            .filter(fileDetail -> fileDetail.getConversionStatus() == FileConversionStatus.FAILED)
                                            .map(FileDetails::getFileName).collect(Collectors.toList()));
                    emailService.sendInternalFailedConversion(internalFailedConversionModel);
                } catch (EmailServiceException ex) {
                    Map<String, Object> debug = new HashMap<>();
                    debug.put("submissionId", submissionId);
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

        repository.updateSubmission(submission);
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

                // retrieve TIFF file
                List<FesFileModel> tiffFiles = new ArrayList<>();
                submission.getFormDetails().getFileDetailsList().forEach(
                        file -> tiffFiles.add(
                                new FesFileModel(
                                        tiffDownloadService.downloadTiffFile(file.getConvertedFileId()), file.getNumberOfPages())));
                LOGGER.debug(String.format("Retrieved [%d] files for submission [%s] from S3", tiffFiles.size(), submission.getId()));

                // insert into FES DB
                fesLoaderService.insertSubmission(new FesLoaderModel(barcode, submission.getCompany().getCompanyName(),
                        submission.getCompany().getCompanyNumber(), submission.getFormDetails().getFormType(),
                        tiffFiles, submittedAt));
                LOGGER.debug(String.format("Inserted submission details into FES DB for submission [%s]",
                        submission.getId()));

                submissionService.updateSubmissionStatus(submission.getId(), SubmissionStatus.SENT_TO_FES);

            } catch (BarcodeException | TiffDownloadException | FesLoaderException | InvalidTiffException ex) {
                LOGGER.errorContext(submission.getId(), "Unable to submit to fes" +
                        ex.getMessage(), ex, null);
            }
        });
    }

    @Override
    public void handleDelayedSubmissions() {
        LocalDateTime now = currentTimestampGenerator.generateTimestamp();
        LocalDateTime supportDelay = now.minusHours(supportDelayInHours);
        LocalDateTime businessDelay = now.minusHours(businessDelayInHours);
        List<Submission> delayedSubmissions = repository.findDelayedSubmissions(SubmissionStatus.PROCESSING, supportDelay);

        List<DelayedSubmissionSupportModel> delayedSubmissionSupportModels =
                delayedSubmissions.stream()
                        .map(submission -> new DelayedSubmissionSupportModel(
                                submission.getId(),
                                submission.getConfirmationReference(),
                                Optional.ofNullable(submission.getSubmittedAt())
                                        .orElseGet(submission::getCreatedAt)
                                        .format(DateTimeFormatter.ofPattern(SUBMITTED_AT_SUPPORT_EMAIL_DATE_FORMAT))))
                        .collect(Collectors.toList());
        if (!delayedSubmissionSupportModels.isEmpty()) {
            emailService.sendDelayedSubmissionSupportEmail(new DelayedSubmissionSupportEmailModel(delayedSubmissionSupportModels));
            Map<String, List<DelayedSubmissionBusinessModel>> delayedSubmissionBusinessModels =
                    delayedSubmissions.stream()
                            .filter(submission -> submission.getLastModifiedAt().isBefore(businessDelay))
                            .map(submission -> new DelayedSubmissionBusinessModel(
                                    submission.getConfirmationReference(),
                                    submission.getCompany().getCompanyNumber(),
                                    submission.getFormDetails().getFormType(),
                                    submission.getPresenter().getEmail(),
                                    Optional.ofNullable(submission.getSubmittedAt())
                                            .orElseGet(submission::getCreatedAt)
                                            .format(DateTimeFormatter.ofPattern(SUBMITTED_AT_BUSINESS_EMAIL_DATE_FORMAT))))
                            .collect(Collectors.groupingBy(delayedSubmissionModel -> formCategoryToEmailAddressService.getEmailAddressForFormCategory(delayedSubmissionModel.getFormType())));
            delayedSubmissionBusinessModels.forEach((key, value) -> emailService.sendDelayedSubmissionBusinessEmail(new DelayedSubmissionBusinessEmailModel(value, key, businessDelayInHours)));
        }
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
