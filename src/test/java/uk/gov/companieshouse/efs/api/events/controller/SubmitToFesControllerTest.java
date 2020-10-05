package uk.gov.companieshouse.efs.api.events.controller;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.companieshouse.efs.api.events.service.EventService;

@ExtendWith(MockitoExtension.class)
public class SubmitToFesControllerTest {

    private SubmitToFesController controller;

    @Mock
    private EventService eventService;

    @BeforeEach
    void setUp() {
        this.controller = new SubmitToFesController(eventService);
    }

    @Test
    void testSubmitToFesControllerInvokesEventService() {
        //when
        controller.submitToFes();

        //then
        verify(eventService).submitToFes();
    }
}
