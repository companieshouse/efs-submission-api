package uk.gov.companieshouse.efs.api.submissions.validator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.text.MessageFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.efs.api.submissions.model.Company;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.submissions.validator.exception.SubmissionValidationException;

@ExtendWith(MockitoExtension.class)
class CompanyDetailsValidatorTest {
    private static final String SUB_ID = "00000000";
    private static final String COMPANY_NUMBER = "NUMBER";
    private static final String COMPANY_NAME = "NAME";

    private CompanyDetailsValidator testValidator;

    @Mock
    private Submission submission;
    @Mock
    private Company company;
    @Mock
    private Validator<Submission> nextValidator;

    @BeforeEach
    void setUp() {
        testValidator = new CompanyDetailsValidator();
        testValidator.setNext(nextValidator);
    }

    @Test
    void validateWhenCompanyNullThenInvalid() {
        when(submission.getId()).thenReturn(SUB_ID);
        
        final SubmissionValidationException exception =
            assertThrows(SubmissionValidationException.class, () -> testValidator.validate(submission));

        assertThat(exception.getMessage(), is(MessageFormat.format("Company details are absent in submission [{0}]",
            SUB_ID)));
        verifyNoInteractions(nextValidator);
    }

    @Test
    void validateWhenCompanyNumberBlankThenInvalid() {
        when(submission.getId()).thenReturn(SUB_ID);
        when(submission.getCompany()).thenReturn(company);
        when(company.getCompanyNumber()).thenReturn("");

        final SubmissionValidationException exception =
            assertThrows(SubmissionValidationException.class, () -> testValidator.validate(submission));

        assertThat(exception.getMessage(), is(MessageFormat.format("Company number is absent in submission [{0}]",
            SUB_ID)));
        verifyNoInteractions(nextValidator);
    }

    @Test
    void validateWhenCompanyNameBlankThenInvalid() {
        when(submission.getId()).thenReturn(SUB_ID);
        when(submission.getCompany()).thenReturn(company);
        when(company.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(company.getCompanyName()).thenReturn("");

        final SubmissionValidationException exception =
            assertThrows(SubmissionValidationException.class, () -> testValidator.validate(submission));

        assertThat(exception.getMessage(), is(MessageFormat.format("Company name is absent in submission [{0}]",
            SUB_ID)));
        verifyNoInteractions(nextValidator);
    }

    @Test
    void validateWhenNumberAndNameNotBlankThenValid() throws SubmissionValidationException {
        when(submission.getCompany()).thenReturn(company);
        when(company.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(company.getCompanyName()).thenReturn(COMPANY_NAME);

        testValidator.validate(submission);

        verify(nextValidator).validate(submission);
    }
}