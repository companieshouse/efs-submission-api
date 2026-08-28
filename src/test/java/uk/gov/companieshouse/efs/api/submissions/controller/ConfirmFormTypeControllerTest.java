package uk.gov.companieshouse.efs.api.submissions.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import uk.gov.companieshouse.api.model.efs.submissions.FileDetailApi;
import uk.gov.companieshouse.api.model.efs.submissions.FileDetailListApi;
import uk.gov.companieshouse.api.model.efs.submissions.FormTypeApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionFormApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionResponseApi;
import uk.gov.companieshouse.efs.api.events.service.S3FileDeleteService;
import uk.gov.companieshouse.efs.api.submissions.service.SubmissionService;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionIncorrectStateException;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionNotFoundException;

@ExtendWith(MockitoExtension.class)
class ConfirmFormTypeControllerTest {

    private static final String SUBMISSION_ID = "submission-id";

    private ConfirmFormTypeController confirmFormTypeController;

    @Mock
    private SubmissionService service;

    @Mock
    private SubmissionResponseApi response;

    @Mock
    private S3FileDeleteService s3FileDeleteService;

    @Mock
    private BindingResult result;

    @Mock
    private SubmissionApi submissionApi;

    @Mock
    private SubmissionFormApi submissionFormApi;

    @Mock
    private FileDetailListApi fileDetailListApi;

    @Mock
    private FileDetailApi fileDetailApiOne;

    @Mock
    private FileDetailApi fileDetailApiTwo;

    @BeforeEach
    void setUp() {
        this.confirmFormTypeController = new ConfirmFormTypeController(service, s3FileDeleteService);
    }

    @Test
    void testConfirmFormTypeReturnsSubmissionId() {
        final var formType = createFormType("NEW-FORM-TYPE");
        stubNoValidationErrors();
        stubSuccessfulFormUpdate();

        final var actual = confirmFormTypeController.confirmFormType(SUBMISSION_ID, formType,
            result);

        assertEquals(response, actual.getBody());
        assertEquals(HttpStatus.OK, actual.getStatusCode());
    }


    @Test
    void testConfirmFormTypeReturns409Conflict() {
        final var formType = createFormType("NEW-FORM-TYPE");
        stubNoValidationErrors();
        when(service.updateSubmissionWithForm(any(), any())).thenThrow(new SubmissionIncorrectStateException("not OPEN"));

        final var actual = confirmFormTypeController.confirmFormType(SUBMISSION_ID, formType, result);

        assertNull(actual.getBody());
        assertEquals(HttpStatus.CONFLICT, actual.getStatusCode());
    }

    @Test
    void testConfirmFormTypeReturns400BadRequest() {
        final var formType = createFormType("NEW-FORM-TYPE");
        when(result.hasErrors()).thenReturn(true);
        when(result.getFieldError()).thenReturn(new FieldError("a", "form.formType", "invalid"));

        final var actual = confirmFormTypeController.confirmFormType(SUBMISSION_ID, formType,
            result);

        assertNull(actual.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode());
    }

    @Test
    void testConfirmFormTypeReturns404NotFound() {
        final var formType = createFormType("NEW-FORM-TYPE");
        stubNoValidationErrors();
        when(service.updateSubmissionWithForm(any(), any())).thenThrow(new SubmissionNotFoundException("not found"));

        final var actual = confirmFormTypeController.confirmFormType(SUBMISSION_ID, formType,
            result);

        assertNull(actual.getBody());
        assertEquals(HttpStatus.NOT_FOUND, actual.getStatusCode());
    }

    @Test
    void shouldDeleteFilesAndClearSubmissionWhenFormTypeChanges() {
        final var formType = createFormType("NEW-FORM-TYPE");
        stubNoValidationErrors();
        stubExistingSubmissionFormType("OLD-FORM-TYPE");
        stubExistingSubmissionFiles("file-1", "file-2");
        stubSuccessfulFormUpdate();

        final var actual = confirmFormTypeController.confirmFormType(SUBMISSION_ID, formType, result);

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(response, actual.getBody());
        verify(s3FileDeleteService).deleteFile(SUBMISSION_ID, "file-1");
        verify(s3FileDeleteService).deleteFile(SUBMISSION_ID, "file-2");
        verify(service).clearSubmissionFiles(SUBMISSION_ID);
    }

    @ParameterizedTest(name = "existing={0}, requested={1}")
    @CsvSource(nullValues = "NULL", value = {
        "SAME-FORM-TYPE, SAME-FORM-TYPE",
        "NULL,           NEW-FORM-TYPE",
        "SH19,           SH19_SAMEDAY",
        "SH19_SAMEDAY,   SH19"
    })
    void shouldNotDeleteFilesWhenFormTypeChangeDoesNotRequireFileDeletion(
            final String existingFormType, final String requestedFormType) {
        final var formType = createFormType(requestedFormType);
        stubNoValidationErrors();
        stubExistingSubmissionFormType(existingFormType);
        stubSuccessfulFormUpdate();

        final var actual = confirmFormTypeController.confirmFormType(SUBMISSION_ID, formType, result);

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        verifyNoInteractions(s3FileDeleteService);
        verify(service, never()).clearSubmissionFiles(any());
    }

    private FormTypeApi createFormType(final String formType) {
        return new FormTypeApi(formType);
    }

    private void stubNoValidationErrors() {
        when(result.hasErrors()).thenReturn(false);
    }

    private void stubSuccessfulFormUpdate() {
        when(service.updateSubmissionWithForm(any(), any())).thenReturn(response);
    }

    private void stubExistingSubmissionFormType(final String existingFormType) {
        when(service.readSubmission(SUBMISSION_ID)).thenReturn(submissionApi);
        when(submissionApi.getSubmissionForm()).thenReturn(submissionFormApi);
        when(submissionFormApi.getFormType()).thenReturn(existingFormType);
    }

    private void stubExistingSubmissionFiles(final String fileIdOne, final String fileIdTwo) {
        when(submissionFormApi.getFileDetails()).thenReturn(fileDetailListApi);
        when(fileDetailListApi.getList()).thenReturn(List.of(fileDetailApiOne, fileDetailApiTwo));
        when(fileDetailApiOne.getFileId()).thenReturn(fileIdOne);
        when(fileDetailApiTwo.getFileId()).thenReturn(fileIdTwo);
    }
}
