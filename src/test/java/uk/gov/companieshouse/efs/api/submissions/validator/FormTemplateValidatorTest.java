package uk.gov.companieshouse.efs.api.submissions.validator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.text.MessageFormat;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.efs.api.formtemplates.model.FormTemplate;
import uk.gov.companieshouse.efs.api.formtemplates.repository.FormTemplateRepository;
import uk.gov.companieshouse.efs.api.submissions.model.FormDetails;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.submissions.validator.exception.SubmissionValidationException;

@ExtendWith(MockitoExtension.class)
class FormTemplateValidatorTest {
    private static final String SUB_ID = "0000000000";
    private static final String TEST_FORM = "FORM";

    private FormTemplateValidator testValidator;

    @Mock
    private FormTemplateRepository formRepository;
    @Mock
    private Submission submission;
    @Mock
    private FormDetails formDetails;
    @Mock
    private Validator<Submission> nextValidator;

    @BeforeEach
    void setUp() {
        testValidator = new FormTemplateValidator(formRepository);
        testValidator.setNext(nextValidator);
    }

    @Test
    void validateWhenRepositoryNullThenValid() throws SubmissionValidationException {
        testValidator = new FormTemplateValidator(null);
        testValidator.setNext(nextValidator);

        testValidator.validate(submission);

        verify(nextValidator).validate(submission);
    }

    @Test
    void validateWhenFormFoundThenValid() throws SubmissionValidationException {
        when(submission.getFormDetails()).thenReturn(formDetails);
        when(formDetails.getFormType()).thenReturn("FORM");
        when(formRepository.findById("FORM")).thenReturn(
            Optional.of(new FormTemplate(null, null, null, null, false, false, null, null)));

        testValidator.validate(submission);

        verify(nextValidator).validate(submission);
    }

    @Test
    void validateWhenFormNotFoundThenInvalid() {
        when(submission.getId()).thenReturn(SUB_ID);
        when(submission.getFormDetails()).thenReturn(formDetails);
        when(formDetails.getFormType()).thenReturn("FORM");
        when(formRepository.findById("FORM")).thenReturn(Optional.empty());

        final SubmissionValidationException exception =
            assertThrows(SubmissionValidationException.class, () -> testValidator.validate(submission));

        assertThat(exception.getMessage(),
            is(MessageFormat.format("Form type [{0}] unknown in submission [{1}]", TEST_FORM, SUB_ID)));
        verifyNoInteractions(nextValidator);
    }

}