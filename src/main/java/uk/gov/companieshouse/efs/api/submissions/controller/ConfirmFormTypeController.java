package uk.gov.companieshouse.efs.api.submissions.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.model.efs.submissions.FileDetailApi;
import uk.gov.companieshouse.api.model.efs.submissions.FileDetailListApi;
import uk.gov.companieshouse.api.model.efs.submissions.FormTypeApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionFormApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionResponseApi;
import uk.gov.companieshouse.efs.api.events.service.S3FileDeleteService;
import uk.gov.companieshouse.efs.api.submissions.service.SubmissionService;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionIncorrectStateException;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionNotFoundException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

/**
 * Handles updates to a submission's form type.
 *
 * <p>When the incoming form type differs from the existing stored form type, any
 * previously uploaded files are deleted from S3 and cleared from the submission
 * before the form type update is applied.</p>
 */
@RestController
@RequestMapping("/efs-submission-api/submission/{id}/form")
public class ConfirmFormTypeController {

    private static final Logger LOGGER = LoggerFactory.getLogger("efs-submission-api");

    private final SubmissionService service;
    private final S3FileDeleteService s3FileDeleteService;

    /**
     * Creates a controller for form type confirmation and related file cleanup.
     *
     * @param service submission service for read/update operations
     * @param s3FileDeleteService S3 service used to delete uploaded submission files
     */
    public ConfirmFormTypeController(final SubmissionService service, final S3FileDeleteService s3FileDeleteService) {
        this.service = service;
        this.s3FileDeleteService = s3FileDeleteService;
    }

    /**
     * Confirms or updates the form type for a submission.
     *
     * <p>If the submission already has a form type and it differs from the requested
     * form type, all existing uploaded files for that submission are deleted from S3 and the submission's stored file
     * list is cleared before the form update.</p>
     * <p>If any S3 delete fails, the error is logged and processing continues on to remove the submission's files list
     * and update the submission's form type.</p>
     *
     * @param id       submission id
     * @param formType requested form type payload
     * @param result   request validation result
     * @return {@link ResponseEntity} containing {@link SubmissionResponseApi} on success, or an empty response with
     *     HTTP 400/404/409 depending on failure mode
     */
    @PutMapping
    public ResponseEntity<SubmissionResponseApi> confirmFormType(@PathVariable final String id,
            @RequestBody @Valid @NotNull final FormTypeApi formType, final BindingResult result) {

        if (result.hasErrors()) {
            final var message = Optional.ofNullable(result.getFieldError())
                                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                                        .orElse(formType.getFormType());
            LOGGER.info("Form type details are invalid: %s".formatted(message));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            final var submission = service.readSubmission(id);
            if (isFormTypeChanged(submission, formType.getFormType())) {
                LOGGER.info("Form type has changed, deleting %s files uploaded for submission with id: [%s]".formatted(
                    submission.getSubmissionForm().getFormType(),
                    id));
                deleteSubmissionFiles(id, submission);
                service.clearSubmissionFiles(id);
            }
            return ResponseEntity.ok(service.updateSubmissionWithForm(id, formType));
        } catch (SubmissionNotFoundException ex) {
            return ResponseEntity.notFound().build();
        } catch (SubmissionIncorrectStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    /**
     * Checks whether the requested form type differs from the currently stored value.
     *
     * @param submission submission being updated
     * @param requestedFormType requested form type value from the request payload
     * @return {@code true} when an existing non-null form type differs from the request
     */
    private boolean isFormTypeChanged(final SubmissionApi submission, final String requestedFormType) {
        return Optional.ofNullable(submission)
            .map(SubmissionApi::getSubmissionForm)
            .map(submissionForm -> submissionForm.getFormType() != null
                && !submissionForm.getFormType().equals(requestedFormType))
            .orElse(false);
    }

    /**
     * Deletes all currently stored submission files from S3 using their file IDs.
     *
     * @param submissionId submission id used for contextual logging
     * @param submission submission containing existing file metadata
     */
    private void deleteSubmissionFiles(final String submissionId, final SubmissionApi submission) {
        final var fileDetails = Optional.ofNullable(submission)
                                        .map(SubmissionApi::getSubmissionForm)
                                        .map(SubmissionFormApi::getFileDetails)
                                        .map(FileDetailListApi::getList)
                                        .orElse(List.of());

        fileDetails.stream()
            .map(FileDetailApi::getFileId)
            .forEach(fileId -> s3FileDeleteService.deleteFile(submissionId, fileId));
    }
}
