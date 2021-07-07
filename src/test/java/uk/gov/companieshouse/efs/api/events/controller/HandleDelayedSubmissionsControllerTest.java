package uk.gov.companieshouse.efs.api.events.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.efs.api.events.service.DelayedSubmissionHandlerContext;
import uk.gov.companieshouse.efs.api.events.service.EventService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

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
        ResponseEntity<Void> actual = controller.handleDelayedSubmissions(null);

        //then
        verify(eventService).handleDelayedSubmissions(DelayedSubmissionHandlerContext.ServiceLevel.STANDARD);
        assertEquals(HttpStatus.OK, actual.getStatusCode());
    }

    @ParameterizedTest
    @EnumSource(DelayedSubmissionHandlerContext.ServiceLevel.class)
    void handleDelayedSubmissions(DelayedSubmissionHandlerContext.ServiceLevel serviceLevel) {
        // when
        ResponseEntity<Void> actual = controller.handleDelayedSubmissions(
            serviceLevel);

        verify(eventService).handleDelayedSubmissions(serviceLevel);
        assertEquals(HttpStatus.OK, actual.getStatusCode());
    }
}