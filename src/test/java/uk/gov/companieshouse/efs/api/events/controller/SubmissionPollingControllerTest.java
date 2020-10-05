package uk.gov.companieshouse.efs.api.events.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import uk.gov.companieshouse.efs.api.events.service.EventService;

@ExtendWith(MockitoExtension.class)
public class SubmissionPollingControllerTest {

    private SubmissionPollingController controller;

    @Mock
    private EventService eventService;

    @BeforeEach
    void setUp() {
        this.controller = new SubmissionPollingController(eventService);
    }

    @Test
    void testSubmissionPollingControllerReturns200OKIfSubmissionsQueuedSuccessfully() {
        //given
        ResponseEntity<Void> actual = controller.queueFiles();

        //then
        assertEquals(HttpStatus.OK, actual.getStatusCode());
        verify(eventService).processFiles();
    }
}
