package uk.gov.companieshouse.efs.api.events.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.efs.api.events.service.EventService;

@RestController
public class HandleDelayedSubmissionsController {
    private EventService eventService;

    @Autowired
    public HandleDelayedSubmissionsController(EventService eventService) {
        this.eventService = eventService;
    }
    
    @PostMapping(value = "/efs-submission-api/events/handle-delayed-submissions")
    public ResponseEntity<Void> handleDelayedSubmissions() {
        this.eventService.handleDelayedSubmissions();

        return ResponseEntity.ok().build();
    }
    
}

