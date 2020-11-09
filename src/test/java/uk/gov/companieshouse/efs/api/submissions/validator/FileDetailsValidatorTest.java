package uk.gov.companieshouse.efs.api.submissions.validator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.text.MessageFormat;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.efs.api.submissions.model.FileDetails;
import uk.gov.companieshouse.efs.api.submissions.model.FormDetails;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.submissions.validator.exception.SubmissionValidationException;

@ExtendWith(MockitoExtension.class)
class FileDetailsValidatorTest {
    private static final String SUB_ID = "0000000000";
    private static final String TEST_FORM = "FORM";

    private FileDetailsValidator testValidator;

    @Mock
    private Submission submission;
    @Mock
    private FormDetails formDetails;
    @Mock
    private Validator<Submission> nextValidator;

    @BeforeEach
    void setUp() {
        testValidator = new FileDetailsValidator();
        testValidator.setNext(nextValidator);
    }

    @Test
    void validateWhenFileDetailsListNullThenInvalid() {
        when(submission.getId()).thenReturn(SUB_ID);
        when(submission.getFormDetails()).thenReturn(formDetails);
        when(formDetails.getFileDetailsList()).thenReturn(null);

        final SubmissionValidationException exception =
            assertThrows(SubmissionValidationException.class, () -> testValidator.validate(submission));

        assertThat(exception.getMessage(),
            is(MessageFormat.format("File details are absent in submission [{0}]", SUB_ID)));
        verifyNoInteractions(nextValidator);
    }

    @Test
    void validateWhenFileDetailsListEmptyThenInvalid() {
        when(submission.getId()).thenReturn(SUB_ID);
        when(submission.getFormDetails()).thenReturn(formDetails);

        final SubmissionValidationException exception =
            assertThrows(SubmissionValidationException.class, () -> testValidator.validate(submission));

        assertThat(exception.getMessage(),
            is(MessageFormat.format("File details are empty in submission [{0}]", SUB_ID)));
        verifyNoInteractions(nextValidator);
    }

    @Test
    void validateWhenFileDetailsListContainsNullThenInvalid() {
        when(submission.getId()).thenReturn(SUB_ID);
        when(submission.getFormDetails()).thenReturn(formDetails);
        when(formDetails.getFileDetailsList()).thenReturn(Arrays.asList(FileDetails.builder().build(), null));

        final SubmissionValidationException exception =
            assertThrows(SubmissionValidationException.class, () -> testValidator.validate(submission));

        assertThat(exception.getMessage(),
            is(MessageFormat.format("File details contains null in submission [{0}]", SUB_ID)));
        verifyNoInteractions(nextValidator);
    }

    @Test
    void validateWhenFileDetailsListContainsNoNullsThenValid() throws SubmissionValidationException {
        when(submission.getFormDetails()).thenReturn(formDetails);
        when(formDetails.getFileDetailsList())
            .thenReturn(Arrays.asList(FileDetails.builder().build(), FileDetails.builder().build()));

        testValidator.validate(submission);
        verify(nextValidator).validate(submission);
    }
}