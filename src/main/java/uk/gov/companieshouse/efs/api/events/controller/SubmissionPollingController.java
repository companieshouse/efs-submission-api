package uk.gov.companieshouse.efs.api.events.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import uk.gov.companieshouse.efs.api.events.service.EventService;


@RestController
public class SubmissionPollingController {

    private EventService eventService;

    @Autowired
    public SubmissionPollingController(EventService eventService) {
        this.eventService = eventService;
    }

    /**
     * Endpoint for queue-files request to process submissions.
     *
     * @return  ResponseEntity&lt;Void&gt;
     */
    @PostMapping(value = "/efs-submission-api/events/queue-files", produces = {"application/json"},
            consumes = {"application/json"})
    public ResponseEntity<Void> queueFiles() {
        this.eventService.processFiles();

        return ResponseEntity.ok().build();
    }
}
