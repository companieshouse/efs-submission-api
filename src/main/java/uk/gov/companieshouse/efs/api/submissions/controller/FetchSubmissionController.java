package uk.gov.companieshouse.efs.api.submissions.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import uk.gov.companieshouse.api.model.efs.submissions.SubmissionApi;
import uk.gov.companieshouse.efs.api.submissions.service.SubmissionService;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/efs-submission-api/submission/{id}")
public class FetchSubmissionController {

    private static final Logger LOGGER = LoggerFactory.getLogger("efs-submission-api");

    private SubmissionService service;

    public FetchSubmissionController(SubmissionService service) {
        this.service = service;
    }

    /**
     * Endpoint for get submission.
     * @param id    submission id
     * @return      ResponseEntity&lt;SubmissionApi&gt;
     */
    @GetMapping
    public ResponseEntity<SubmissionApi> fetchSubmission(@PathVariable String id) {

        SubmissionApi submission = service.readSubmission(id);

        // check if submission exists
        if (submission == null) {
            Map<String, Object> debug = new HashMap<>();
            debug.put("submissionId", id);
            LOGGER.errorContext(id, "Could not locate submission", null, debug);
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(submission);
    }

}
