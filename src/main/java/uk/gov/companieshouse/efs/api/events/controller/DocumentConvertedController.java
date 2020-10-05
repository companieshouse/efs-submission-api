package uk.gov.companieshouse.efs.api.events.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import uk.gov.companieshouse.api.model.efs.events.FileConversionStatusApi;
import uk.gov.companieshouse.efs.api.events.service.EventService;
import uk.gov.companieshouse.efs.api.submissions.service.exception.FileIncorrectStateException;
import uk.gov.companieshouse.efs.api.submissions.service.exception.FileNotFoundException;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionIncorrectStateException;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionNotFoundException;

@RestController
public class DocumentConvertedController {

    private EventService eventService;

    @Autowired
    public DocumentConvertedController(EventService eventService) {
        this.eventService = eventService;
    }

    /**
     * Endpoint for file conversion status.
     *
     * @param submissionId              submission id
     * @param fileId                    file id
     * @param fileConversionStatusApi   conversion status details
     * @param request                   the request
     * @return                          ResponseEntity&lt;Void&gt;
     */
    @PostMapping(value = "/efs-submission-api/events/submissions/{submission_id}/files/{file_id}", produces = {"application/json"},
            consumes = {"application/json"})
    public ResponseEntity<Void> updateFileConversionStatus(@PathVariable("submission_id") String submissionId,
                                                           @PathVariable("file_id") String fileId,
                                                           @RequestBody FileConversionStatusApi fileConversionStatusApi,
                                                           HttpServletRequest request) {

        // update the file in the submission with the file status and converted file id
        // and check all file statuses in the submission
        // if all CONVERTED then change submission status to be READY_FOR_FES
        // if at least one file is in a status of FAILED, and all other files (if more than one present) have been
        // CONVERTED or FAILED then change submission status to be REJECTED_BY_DOCUMENT_CONVERTER and send
        // an internal email
        try {
            this.eventService.updateConversionFileStatus(submissionId, fileId, fileConversionStatusApi);
        } catch (SubmissionNotFoundException | FileNotFoundException ex) {
            // Return a non-200 (404) if submission / file id doesn't exist
            return ResponseEntity.notFound().build();
        } catch (SubmissionIncorrectStateException | FileIncorrectStateException ex) {
            // Return a 409 if submission / file conversion status (!= QUEUED) is in the
            // wrong state
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }


        return ResponseEntity.ok().build();
    }


}
