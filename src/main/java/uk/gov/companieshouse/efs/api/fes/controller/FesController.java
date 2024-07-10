package uk.gov.companieshouse.efs.api.fes.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.model.efs.fes.StatusApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionResponseApi;
import uk.gov.companieshouse.efs.api.fes.service.FesService;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionIncorrectStateException;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionNotFoundException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
public class FesController {

    private FesService fesService;

    private static final Logger LOGGER = LoggerFactory.getLogger("efs-submission-api");

    @Autowired
    public FesController(FesService fesService) {
        this.fesService = fesService;
    }

    /**
     * Endpoint for updating submission.
     *
     * @param barcode   barcode of submission
     * @param status    submission status
     * @param result    bindingResult
     * @return          ResponseEntity&lt;SubmissionResponseApi&gt;
     */
    @PostMapping(value = "/efs-submission-api/fes/submissions/{barcode}/complete", produces = {"application/json"},
            consumes = {"application/json"})
    public ResponseEntity<SubmissionResponseApi> updateSubmissionStatusByBarcode(@PathVariable("barcode") String barcode,
            @RequestBody @Valid StatusApi status, BindingResult result) {

        if (result.hasErrors()) {
            Map<String, Object> debug = new HashMap<>();
            debug.put("status", status);
            debug.put("errorMessage", Optional.ofNullable(result.getFieldError())
                    .map(FieldError::getDefaultMessage).orElse(""));
            LOGGER.errorContext(barcode,
                    "updateSubmissionStatusByBarcode: Invalid status provided", null, debug);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            fesService.updateSubmissionStatusByBarcode(barcode, status.getStatus());
        } catch (SubmissionNotFoundException ex) {
            return ResponseEntity.notFound().build();
        } catch (SubmissionIncorrectStateException se) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        return ResponseEntity.ok().build();
    }

}
