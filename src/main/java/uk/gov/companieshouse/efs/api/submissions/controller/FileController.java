package uk.gov.companieshouse.efs.api.submissions.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateApi;
import uk.gov.companieshouse.api.model.efs.submissions.FileListApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionFormApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionResponseApi;
import uk.gov.companieshouse.efs.api.formtemplates.service.FormTemplateService;
import uk.gov.companieshouse.efs.api.submissions.service.SubmissionService;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionIncorrectStateException;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionNotFoundException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@RestController
@RequestMapping("/efs-submission-api/submission/{id}/files")
public class FileController {

    private static final Logger LOGGER = LoggerFactory.getLogger("efs-submission-api");
    private static final int FES_ENABLED_FORM_MAX_FILES = 1;
    private static final int NON_FES_ENABLED_MAX_FILES = 10;

    private final SubmissionService submissionService;
    private final FormTemplateService formTemplateService;


    public FileController(final SubmissionService submissionService,
        final FormTemplateService formTemplateService) {
        this.submissionService = submissionService;
        this.formTemplateService = formTemplateService;
    }

    /**
     * Endpoint to upload file details.
     *
     * @param id        submission id
     * @param files     list of file details
     * @param result    bindingResult
     * @return          {@link ResponseEntity<SubmissionResponseApi>};
     */
    @PutMapping
    public ResponseEntity<SubmissionResponseApi> uploadFile(@PathVariable String id,
                                                            @RequestBody @Valid @NotNull FileListApi files, BindingResult result) {

        if (result.hasErrors()) {
            final var errorMessage = result.getAllErrors().stream().map(
                DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.joining(", "));
            LOGGER.info("File list details are invalid: %s".formatted(errorMessage));

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        if (maxFileCountExceeded(id, files)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            return ResponseEntity.ok(submissionService.updateSubmissionWithFileDetails(id, files));
        } catch (SubmissionNotFoundException ex) {
            return ResponseEntity.notFound().build();
        } catch (SubmissionIncorrectStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

    }

    private boolean maxFileCountExceeded(final String id, final @NotNull FileListApi files) {
        final var filesCount = files.getFiles().size();
        final var maxFiles = Optional.ofNullable(submissionService.readSubmission(id))
            .map(SubmissionApi::getSubmissionForm)
            .map(SubmissionFormApi::getFormType)
            .map(formTemplateService::getFormTemplate)
            .filter(FormTemplateApi::isFesEnabled)
            .map(template -> FES_ENABLED_FORM_MAX_FILES)
            .orElse(NON_FES_ENABLED_MAX_FILES);
        if (filesCount > maxFiles) {
            LOGGER.info("File list details are invalid: maximum file count %d exceeded for submission with id: [%s]".formatted(maxFiles, id));
            return true;
        }
        return false;
    }
}
