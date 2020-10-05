package uk.gov.companieshouse.efs.api.events.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import uk.gov.companieshouse.api.model.efs.events.FileConversionResultStatusApi;
import uk.gov.companieshouse.api.model.efs.events.FileConversionStatusApi;
import uk.gov.companieshouse.api.model.efs.submissions.FileConversionStatus;
import uk.gov.companieshouse.efs.api.events.service.EventService;
import uk.gov.companieshouse.efs.api.submissions.service.exception.FileIncorrectStateException;
import uk.gov.companieshouse.efs.api.submissions.service.exception.FileNotFoundException;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionIncorrectStateException;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionNotFoundException;

@ExtendWith(MockitoExtension.class)
public class DocumentConvertedControllerTest {

    private static final int NUMBER_OF_PAGES = 100;

    private DocumentConvertedController controller;

    @Mock
    private EventService service;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        this.controller = new DocumentConvertedController(service);
    }

    @Test
    void testNotifyFileConverted() {
        //given
        FileConversionStatusApi conversionStatus = new FileConversionStatusApi("999", FileConversionResultStatusApi.CONVERTED,
                100);

        //when
        ResponseEntity<Void> actual = this.controller.updateFileConversionStatus("abc", "123", conversionStatus, request);

        //then
        assertEquals(HttpStatus.OK, actual.getStatusCode());
        verify(service).updateConversionFileStatus("abc", "123", conversionStatus);
    }

    @Test
    void testDocumentConvertedControllerReturnsA404WhenSubmissionNotFoundExceptionIsThrown() {
        // given
        FileConversionStatusApi conversionStatus = new FileConversionStatusApi("999", FileConversionResultStatusApi.CONVERTED, NUMBER_OF_PAGES);

        //when
        doThrow(SubmissionNotFoundException.class).when(service).updateConversionFileStatus("abc", "123",
                conversionStatus);

        ResponseEntity<Void> actual = controller.updateFileConversionStatus("abc", "123", conversionStatus, request);

        // then
        assertEquals(HttpStatus.NOT_FOUND, actual.getStatusCode());
        verify(service).updateConversionFileStatus("abc", "123", conversionStatus);
    }

    @Test
    void testDocumentConvertedControllerReturnsA409WhenFileNotFoundExceptionIsThrown() {
        // given
        FileConversionStatusApi conversionStatus = new FileConversionStatusApi("999", FileConversionResultStatusApi.CONVERTED, NUMBER_OF_PAGES);

        // when
        doThrow(FileNotFoundException.class).when(service).updateConversionFileStatus("abc", "123", conversionStatus);

        ResponseEntity<Void> actual = controller.updateFileConversionStatus("abc", "123", conversionStatus, request);

        // then
        assertEquals(HttpStatus.NOT_FOUND, actual.getStatusCode());
        verify(service).updateConversionFileStatus("abc", "123", conversionStatus);
    }

    @Test
    void testDocumentConvertedControllerReturnsA409WhenSubmissionIncorrectStateExceptionIsThrown() {
        // given
        FileConversionStatusApi conversionStatus = new FileConversionStatusApi("999", FileConversionResultStatusApi.CONVERTED, NUMBER_OF_PAGES);

        // when
        doThrow(SubmissionIncorrectStateException.class).when(service).updateConversionFileStatus("abc", "123",
                conversionStatus);

        ResponseEntity<Void> actual = controller.updateFileConversionStatus("abc", "123", conversionStatus, request);

        // then
        assertEquals(HttpStatus.CONFLICT, actual.getStatusCode());
        verify(service).updateConversionFileStatus("abc", "123", conversionStatus);
    }

    @Test
    void testDocumentConvertedControllerReturnsA409WhenFileIncorrectStateExceptionIsThrown() {
        // given
        FileConversionStatusApi conversionStatus = new FileConversionStatusApi("999", FileConversionResultStatusApi.CONVERTED, NUMBER_OF_PAGES);

        // when
        doThrow(FileIncorrectStateException.class).when(service).updateConversionFileStatus("abc", "123",
                conversionStatus);

        ResponseEntity<Void> actual = controller.updateFileConversionStatus("abc", "123", conversionStatus, request);

        // then
        assertEquals(HttpStatus.CONFLICT, actual.getStatusCode());
        verify(service).updateConversionFileStatus("abc", "123", conversionStatus);
    }
}
