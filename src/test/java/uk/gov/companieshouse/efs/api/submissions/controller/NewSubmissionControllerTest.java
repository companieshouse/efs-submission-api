package uk.gov.companieshouse.efs.api.submissions.controller;

import static org.junit.Assert.assertEquals;
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

import uk.gov.companieshouse.api.model.efs.submissions.SubmissionResponseApi;
import uk.gov.companieshouse.efs.api.submissions.service.SubmissionService;
import uk.gov.companieshouse.efs.api.submissions.model.PresenterApi;

@ExtendWith(MockitoExtension.class)
class NewSubmissionControllerTest {

    @Mock
    private SubmissionResponseApi response;

    private NewSubmissionController controller;

    @Mock
    private SubmissionService submissionService;

    @Mock
    private BindingResult result;

    @Mock
    private PresenterApi presenter;

    @BeforeEach
    public void setUp() {
        controller = new NewSubmissionController(submissionService);
    }

    @Test
    void testNewSubmissionReturnsId() {
        //given
        when(submissionService.createSubmission(presenter)).thenReturn(response);

        //when
        ResponseEntity<SubmissionResponseApi> actual = controller.newSubmission(presenter, result);

        //then
        assertEquals(response, actual.getBody());
        assertEquals(HttpStatus.CREATED, actual.getStatusCode());
    }

    @Test
    void testReturnBadRequestIfPresenterEmailAbsent() {
        //given
        when(result.hasErrors()).thenReturn(true);
        when(result.getFieldError()).thenReturn(new FieldError("a", "presenter.email", "invalid"));

        //when
        ResponseEntity<SubmissionResponseApi> actual = controller.newSubmission(presenter, result);

        //then
        assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode());
    }
}
