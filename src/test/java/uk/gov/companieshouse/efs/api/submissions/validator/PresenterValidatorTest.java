package uk.gov.companieshouse.efs.api.submissions.validator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.text.MessageFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.efs.api.submissions.model.Presenter;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.submissions.validator.exception.SubmissionValidationException;

@ExtendWith(MockitoExtension.class)
class PresenterValidatorTest {
    private static final String SUB_ID = "0000000000";

    private PresenterValidator testValidator;

    @Mock
    private Submission submission;
    @Mock
    private Presenter presenter;
    @Mock
    private Validator<Submission> nextValidator;

    @BeforeEach
    void setUp() {
        testValidator = new PresenterValidator();
        testValidator.setNext(nextValidator);
    }

    @Test
    void validateWhenPresenterNullThenInvalid() {
        when(submission.getId()).thenReturn(SUB_ID);

        final SubmissionValidationException exception =
            assertThrows(SubmissionValidationException.class, () -> testValidator.validate(submission));

        assertThat(exception.getMessage(),
            is(MessageFormat.format("Presenter details are absent in submission [{0}]", SUB_ID)));
        verifyNoMoreInteractions(nextValidator);
    }

    @Test
    void validateWhenPresenterEmailNullThenInvalid() {
        when(submission.getPresenter()).thenReturn(presenter);

        assertThrows(SubmissionValidationException.class, () -> testValidator.validate(submission));
        verifyNoMoreInteractions(nextValidator);
    }
    @Test
    void validateWhenPresenterBlankThenValid() throws SubmissionValidationException {
        when(submission.getPresenter()).thenReturn(presenter);
        when(presenter.getEmail()).thenReturn("A");

        testValidator.validate(submission);
        verify(nextValidator).validate(submission);
    }
}