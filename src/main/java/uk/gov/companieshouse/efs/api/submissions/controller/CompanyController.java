package uk.gov.companieshouse.efs.api.submissions.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
import uk.gov.companieshouse.api.model.efs.submissions.CompanyApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionResponseApi;
import uk.gov.companieshouse.efs.api.submissions.service.SubmissionService;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionIncorrectStateException;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionNotFoundException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

/**
 * Add or updates company information on the application.
 */
@RestController
@RequestMapping(value = "/efs-submission-api/submission/{id}/company", produces = {"application/json"},
        consumes = {"application/json"})
public class CompanyController {

    private static final Logger LOGGER = LoggerFactory.getLogger("efs-submission-api");

    private SubmissionService service;

    public CompanyController(SubmissionService service) {
        this.service = service;
    }

    /**
     * Endpoint for company details.
     *
     * @param id        submission id
     * @param company   company details
     * @param result    bindingResult
     * @return          ResponseEntity&lt;SubmissionResponseApi&gt;
     */
    @PutMapping
    public ResponseEntity<SubmissionResponseApi> submitCompany(@PathVariable final String id,
                                                               @RequestBody @Valid @NotNull final CompanyApi company, final BindingResult result) {
        if (result.hasErrors()) {
            LOGGER.info("Company details are invalid: %s".formatted(result.getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.joining(","))));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            return ResponseEntity.ok(service.updateSubmissionWithCompany(id, company));
        } catch (SubmissionNotFoundException ex) {
            return ResponseEntity.notFound().build();
        } catch (SubmissionIncorrectStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

    }

}
