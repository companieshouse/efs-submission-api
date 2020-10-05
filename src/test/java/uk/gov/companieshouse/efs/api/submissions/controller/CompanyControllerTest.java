package uk.gov.companieshouse.efs.api.submissions.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import uk.gov.companieshouse.api.model.efs.submissions.CompanyApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionResponseApi;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.submissions.service.SubmissionService;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionIncorrectStateException;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionNotFoundException;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CompanyControllerTest {

	@Mock
	private SubmissionResponseApi response;

	private CompanyController controller;

	@Mock
	private SubmissionService service;

	@Mock
	private Submission submission;
	
    @Mock
    private BindingResult result;

    @Mock
    private @Valid @NotNull CompanyApi company;

	@BeforeEach
	public void setUp() {
        controller = new CompanyController(service);
	}

	@Test
	public void testSubmitCompanyReturnsId() {
		//given
		when(service.updateSubmissionWithCompany(any(), any())).thenReturn(response);

		//when
        ResponseEntity<SubmissionResponseApi> actual = controller.submitCompany("123", company, result);

		//then
		assertEquals(response, actual.getBody());
		assertEquals(HttpStatus.OK, actual.getStatusCode());
	}

	@Test
	public void testSubmitCompanyReturns404NotFound() {
		//given
		when(service.updateSubmissionWithCompany(any(), any())).thenThrow(new SubmissionNotFoundException("not found"));

		//when
		ResponseEntity<SubmissionResponseApi> actual = controller.submitCompany("123", company, result);

		//then
		assertNull(actual.getBody());
		assertEquals(HttpStatus.NOT_FOUND, actual.getStatusCode());
	}

	@Test
	public void testSubmitCompanyReturns409Conflict() {
		//given
		when(service.updateSubmissionWithCompany(any(), any())).thenThrow(new SubmissionIncorrectStateException("not OPEN"));

		//when
        ResponseEntity<SubmissionResponseApi> actual = controller.submitCompany("123", company, result);

		//then
		assertNull(actual.getBody());
		assertEquals(HttpStatus.CONFLICT, actual.getStatusCode());
	}

    @Test
    public void testSubmitCompanyReturns400BadRequest() {
        // given
        when(result.hasErrors()).thenReturn(true);
        when(result.getAllErrors()).thenReturn(Arrays.asList(new FieldError("a", "company.name", "invalid"),
                new FieldError("a", "company.number", "invalid")));


        // when
        ResponseEntity<SubmissionResponseApi> actual = controller.submitCompany("123", company, result);

        // then
        assertNull(actual.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode());
    }

}
