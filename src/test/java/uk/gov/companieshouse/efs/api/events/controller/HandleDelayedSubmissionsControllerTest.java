package uk.gov.companieshouse.efs.api.events.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.efs.api.events.service.EventService;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class HandleDelayedSubmissionsControllerTest {

    private HandleDelayedSubmissionsController controller;

    @Mock
    private EventService eventService;

    @BeforeEach
    void setUp() {
        this.controller = new HandleDelayedSubmissionsController(eventService);
    }

    @Test
    void testControllerReturns200OKIfNoExceptionThrownByEventService() {
        //when
        ResponseEntity<Void> actual = controller.handleDelayedSubmissions();

        //then
        assertEquals(HttpStatus.OK, actual.getStatusCode());
    }
}