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
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.submissions.validator.exception.SubmissionValidationException;

@ExtendWith(MockitoExtension.class)
class ConfirmationReferenceValidatorTest {
    private static final String SUB_ID = "00000000";

    private ConfirmationReferenceValidator testValidator;

    @Mock
    private Submission submission;
    @Mock
    private Validator<Submission> nextValidator;

    @BeforeEach
    void setUp() {
        testValidator = new ConfirmationReferenceValidator();
        testValidator.setNext(nextValidator);
    }

    @Test
    void validateWhenConfirmationReferenceNullThenInvalid() {
        when(submission.getId()).thenReturn(SUB_ID);

        final SubmissionValidationException exception =
            assertThrows(SubmissionValidationException.class, () -> testValidator.validate(submission));

        assertThat(exception.getMessage(), is(MessageFormat.format("Confirmation reference is absent in submission [{0}]",
            SUB_ID)));
        verifyNoInteractions(nextValidator);
    }

    @Test
    void validateWhenConfirmationReferenceBlankThenInvalid() {
        when(submission.getId()).thenReturn(SUB_ID);
        when(submission.getConfirmationReference()).thenReturn("");

        final SubmissionValidationException exception =
            assertThrows(SubmissionValidationException.class, () -> testValidator.validate(submission));

        assertThat(exception.getMessage(), is(MessageFormat.format("Confirmation reference is absent in submission [{0}]",
            SUB_ID)));
        verifyNoInteractions(nextValidator);
    }

    @Test
    void validateWhenConfirmationReferenceNotBlankThenValid() throws SubmissionValidationException {
        when(submission.getConfirmationReference()).thenReturn("AAAAA");

        testValidator.validate(submission);

        verify(nextValidator).validate(submission);
    }
}