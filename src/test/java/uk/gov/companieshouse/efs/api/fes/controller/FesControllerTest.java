package uk.gov.companieshouse.efs.api.fes.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import uk.gov.companieshouse.api.model.efs.fes.StatusApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionResponseApi;
import uk.gov.companieshouse.efs.api.fes.service.FesService;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionIncorrectStateException;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FesControllerTest {

    private static final String BARCODE = "123";

    private FesController fesController;

    @Mock
    private FesService fesService;

    @Mock
    private StatusApi statusApi;

    @Mock
    private BindingResult result;

    @BeforeEach
    void setUp() {
        this.fesController = new FesController(fesService);
    }

    @Test
    void testUpdateSubmissionStatusByBarcode() {
        //given

        //when
        ResponseEntity<SubmissionResponseApi> actual =
                fesController.updateSubmissionStatusByBarcode("123", statusApi, result);

        //then
        assertEquals(HttpStatus.OK, actual.getStatusCode());
        verify(fesService).updateSubmissionStatusByBarcode(BARCODE, statusApi.getStatus());
    }


    @Test
    void testUpdateSubmissionStatusByBarcodeReturns400WhenStatusIsNull() {
        // given
        when(result.hasErrors()).thenReturn(true);
        when(result.getFieldError()).thenReturn(new FieldError("a", "status", "invalid"));

        // when
        ResponseEntity<SubmissionResponseApi> actual = fesController.updateSubmissionStatusByBarcode("123", statusApi,
                result);

        // then
        assertNull(actual.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode());
    }

    @Test
    void testUpdateSubmissionStatusByBarcodeNotFound() {
        //given
        when(result.hasErrors()).thenReturn(false);
        doThrow(SubmissionNotFoundException.class).when(fesService).updateSubmissionStatusByBarcode(any(), any());

        //when
        ResponseEntity<SubmissionResponseApi> actual =
                fesController.updateSubmissionStatusByBarcode("123", statusApi, result);

        //then
        assertNull(actual.getBody());
        assertEquals(HttpStatus.NOT_FOUND, actual.getStatusCode());
        verify(fesService).updateSubmissionStatusByBarcode(any(), any());
    }

    @Test
    void testUpdateSubmissionStatusByBarcodeInConflict() {
        //given
        doThrow(SubmissionIncorrectStateException.class).when(fesService).updateSubmissionStatusByBarcode(any(), any());

        //when
        ResponseEntity<SubmissionResponseApi> actual =
                fesController.updateSubmissionStatusByBarcode("123", statusApi, result);

        //then
        assertNull(actual.getBody());
        assertEquals(HttpStatus.CONFLICT, actual.getStatusCode());
        verify(fesService).updateSubmissionStatusByBarcode(any(), any());
    }
}
