package uk.gov.companieshouse.efs.api.submissions.controller;

import java.util.stream.Collectors;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.model.efs.submissions.FileListApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionResponseApi;
import uk.gov.companieshouse.efs.api.submissions.service.SubmissionService;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionIncorrectStateException;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionNotFoundException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@RestController
@RequestMapping("/efs-submission-api/submission/{id}/files")
public class FileController {

    private static final Logger LOGGER = LoggerFactory.getLogger("efs-submission-api");

    private SubmissionService service;


    public FileController(SubmissionService service) {
        this.service = service;
    }

    /**
     * Endpoint to upload file details.
     *
     * @param id        submission id
     * @param files     lits of file details
     * @param result    bindingResult
     * @return          ResponseEntity&lt;SubmissionResponseApi&gt;
     */
    @PutMapping
    public ResponseEntity<SubmissionResponseApi> uploadFile(@PathVariable String id,
                                                            @RequestBody @Valid @NotNull FileListApi files, BindingResult result) {

        if (result.hasErrors()) {
            LOGGER.info(String.format("File list details are invalid: %s", result.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.joining(","))));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            return ResponseEntity.ok(service.updateSubmissionWithFileDetails(id, files));
        } catch (SubmissionNotFoundException ex) {
            return ResponseEntity.notFound().build();
        } catch (SubmissionIncorrectStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

    }
}
