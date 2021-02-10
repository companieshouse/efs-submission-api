package uk.gov.companieshouse.efs.api.submissions.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import uk.gov.companieshouse.api.model.efs.submissions.FileListApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionResponseApi;
import uk.gov.companieshouse.efs.api.submissions.service.SubmissionService;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionIncorrectStateException;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionNotFoundException;

import java.util.Arrays;

@ExtendWith(MockitoExtension.class)
class FileControllerTest {

    @Mock
    private FileListApi files;

    private FileController controller;

    @Mock
    private SubmissionService service;

    @Mock
    private SubmissionResponseApi response;

    @Mock
    private BindingResult result;

    @BeforeEach
    void setUp() {
        this.controller = new FileController(service);
    }

    @Test
    void testUploadFile() {
        //given
        when(service.updateSubmissionWithFileDetails(any(), any())).thenReturn(response);

        //when
        ResponseEntity<SubmissionResponseApi> actual = controller.uploadFile("123", files, result);

        //then
        assertEquals(response, actual.getBody());
        assertEquals(HttpStatus.OK, actual.getStatusCode());
    }

    @Test
    void testUploadFileReturns409Conflict() {
        //given
        when(service.updateSubmissionWithFileDetails(any(), any())).thenThrow(new SubmissionIncorrectStateException("not OPEN"));

        //when
        ResponseEntity<SubmissionResponseApi> actual = controller.uploadFile("123", files, result);

        //then
        assertNull(actual.getBody());
        assertEquals(HttpStatus.CONFLICT, actual.getStatusCode());
    }

    @Test
    void testUploadFileReturns400BadRequest() {
        // given
        when(result.hasErrors()).thenReturn(true);
        when(result.getAllErrors()).thenReturn(Arrays.asList(new FieldError("a", "files[0].file_id", "invalid"), new FieldError("a", "files[0].file_size", "invalid")));

        // when
        ResponseEntity<SubmissionResponseApi> actual = controller.uploadFile("123", files, result);

        // then
        assertNull(actual.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode());
    }

    @Test
    void testUploadFileReturns404NotFound() {
        // given
        when(service.updateSubmissionWithFileDetails(any(), any())).thenThrow(new SubmissionNotFoundException("not found"));

        // when
        ResponseEntity<SubmissionResponseApi> actual = controller.uploadFile("123", files, result);

        // then
        assertNull(actual.getBody());
        assertEquals(HttpStatus.NOT_FOUND, actual.getStatusCode());
    }

}
