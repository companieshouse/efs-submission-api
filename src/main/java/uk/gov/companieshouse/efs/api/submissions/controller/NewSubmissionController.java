package uk.gov.companieshouse.efs.api.submissions.controller;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import uk.gov.companieshouse.api.model.efs.submissions.PresenterApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionResponseApi;
import uk.gov.companieshouse.efs.api.submissions.service.SubmissionService;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@Validated
@RequestMapping("/efs-submission-api/submissions/new")
public class NewSubmissionController {

    private static final Logger LOGGER = LoggerFactory.getLogger("efs-submission-api");

    private SubmissionService submissionService;

    @Autowired
    public NewSubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    /**
     * Endpoint for new submission.
     *
     * @param presenterApi  presenter details
     * @param result        bindingResult
     * @return              ResponseEntity&lt;SubmissionResponseApi&gt;
     */
    @PostMapping
    public ResponseEntity<SubmissionResponseApi> newSubmission(@RequestBody @Valid @NotNull PresenterApi presenterApi,
                                                               BindingResult result) {

        if (result.hasErrors()) {
            Map<String, Object> debug = new HashMap<>();
            debug.put("fieldError", Optional.ofNullable(result.getFieldError())
            .map(FieldError::getDefaultMessage).orElse("Unable to get field error"));
            LOGGER.error("Unable to create new submission. Presenter details are invalid",
                    null, debug);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        SubmissionResponseApi id = this.submissionService.createSubmission(presenterApi);

        return new ResponseEntity<>(id, HttpStatus.CREATED);
    }

}
