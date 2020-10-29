package uk.gov.companieshouse.efs.api.submissions.validator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.text.MessageFormat;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.efs.api.categorytemplates.service.CategoryTemplateService;
import uk.gov.companieshouse.efs.api.formtemplates.model.FormTemplate;
import uk.gov.companieshouse.efs.api.formtemplates.repository.FormTemplateRepository;
import uk.gov.companieshouse.efs.api.submissions.model.FormDetails;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.submissions.validator.exception.SubmissionValidationException;

@ExtendWith(MockitoExtension.class)
class ConfirmAuthorisedValidatorTest {
    private static final String SUB_ID = "0000000000";
    private static final String TEST_FORM = "FORM";

    private ConfirmAuthorisedValidator testValidator;

    @Mock
    private FormTemplateRepository formRepository;
    @Mock
    private CategoryTemplateService categoryService;
    @Mock
    private Submission submission;
    @Mock
    private FormTemplate formTemplate;
    @Mock
    private FormDetails formDetails;

    @Test
    void validateWhenCategoryServiceNullThenValid() throws SubmissionValidationException {
        testValidator = new ConfirmAuthorisedValidator(formRepository, null);
        testValidator.setNext(nextValidator);

        testValidator.validate(submission);

        verify(nextValidator).validate(submission);
    }

    @Test
    void validateWhenTopLevelCategoryNotInsolvencyThenValid() throws SubmissionValidationException {
        testValidator = new ConfirmAuthorisedValidator(formRepository, categoryService);
        testValidator.setNext(nextValidator);

        testValidator.validate(submission);

        verify(nextValidator).validate(submission);
    }

    @Mock
    private Validator<Submission> nextValidator;

    @Test
    void validateWhenInsolvencyAndConfirmAuthorisedNullThenInvalid() {
        testValidator = new ConfirmAuthorisedValidator(formRepository, categoryService);
        testValidator.setNext(nextValidator);
        when(submission.getId()).thenReturn(SUB_ID);
        when(submission.getFormDetails()).thenReturn(formDetails);
        when(formDetails.getFormType()).thenReturn(TEST_FORM);
        when(formTemplate.getFormType()).thenReturn(TEST_FORM);
        when(formRepository.findById(TEST_FORM)).thenReturn(Optional.of(formTemplate));

        final SubmissionValidationException exception =
            assertThrows(SubmissionValidationException.class, () -> testValidator.validate(submission));

        assertThat(exception.getMessage(), is(MessageFormat
            .format("Presenter must confirm they are authorised in submission [{0}] for Insolvency form [{1}]", SUB_ID,
                TEST_FORM)));
        verifyNoInteractions(nextValidator);
    }

    @Test
    void validateWhenConfirmAuthorisedNullThenValid() throws SubmissionValidationException {
        testValidator = new ConfirmAuthorisedValidator(formRepository, categoryService);
        testValidator.setNext(nextValidator);

        testValidator.validate(submission);

        verify(nextValidator).validate(submission);
    }

    @Test
    void validateWhenConfirmAuthorisedTrueThenValid() throws SubmissionValidationException {
        when(submission.getConfirmAuthorised()).thenReturn(true);
        testValidator = new ConfirmAuthorisedValidator(formRepository, categoryService);
        testValidator.setNext(nextValidator);

        testValidator.validate(submission);

        verify(nextValidator).validate(submission);
    }
}