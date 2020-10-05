package uk.gov.companieshouse.efs.api.events.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import uk.gov.companieshouse.efs.api.events.service.EventService;

@RestController
@RequestMapping("/efs-submission-api/events")
public class SubmitToFesController {

    private EventService eventService;

    @Autowired
    public SubmitToFesController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping("/submit-files-to-fes")
    public ResponseEntity<Void> submitToFes() {
        eventService.submitToFes();
        return ResponseEntity.ok().build();
    }
}
