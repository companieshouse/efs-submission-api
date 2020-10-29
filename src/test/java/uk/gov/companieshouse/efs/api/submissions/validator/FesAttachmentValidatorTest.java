package uk.gov.companieshouse.efs.api.submissions.validator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;
import org.apache.commons.lang3.BooleanUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.efs.api.formtemplates.model.FormTemplate;
import uk.gov.companieshouse.efs.api.formtemplates.repository.FormTemplateRepository;
import uk.gov.companieshouse.efs.api.submissions.model.FileDetails;
import uk.gov.companieshouse.efs.api.submissions.model.FormDetails;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.submissions.validator.exception.SubmissionValidationException;

@ExtendWith(MockitoExtension.class)
class FesAttachmentValidatorTest {
    private static final String SUB_ID = "0000000000";
    private static final String FEE_FORM = "FEE_FORM";
    private static final String NO_FEE_FORM = "NO_FEE_FORM";

    private FesAttachmentValidator testValidator;

    @Mock
    private FormTemplateRepository formRepository;
    @Mock
    private Submission submission;
    @Mock
    private FormTemplate formTemplate;
    @Mock
    private FormDetails formDetails;
    @Mock
    private FileDetails fileDetails;
    @Mock
    private Validator<Submission> nextValidator;

    @BeforeEach
    void setUp() {
        testValidator = new FesAttachmentValidator(formRepository);
        testValidator.setNext(nextValidator);
    }

    @Test
    void validateWhenFormRepositoryNullThenValid() throws SubmissionValidationException {
        testValidator = new FesAttachmentValidator(null);
        testValidator.setNext(nextValidator);

        testValidator.validate(submission);
        verify(nextValidator).validate(submission);
    }

    private static Stream<Arguments> provideValidPreconditions() {
        return Stream.of(Arguments.of(false, null, null), // test FormTemplate not found
            Arguments.of(false, false, false), Arguments.of(false, false, true), Arguments.of(false, true, false),
            Arguments.of(false, true, true), Arguments.of(true, false, false), Arguments.of(true, true, false));
    }

    private static Stream<Arguments> provideInvalidPreconditions() {
        return Stream.of(Arguments.of(true, false, true), Arguments.of(true, true, true));
    }

    @ParameterizedTest(name = "[{index}] att={0}, fee={1}, fes={2}")
    @MethodSource("provideValidPreconditions")
    void validateWhenPreconditionsSatisfied(final boolean hasAttachments, final Boolean hasFee,
        final Boolean fesEnabled) throws SubmissionValidationException {
        expectPreconditions(hasAttachments, hasFee, fesEnabled);

        testValidator.validate(submission);
        verify(nextValidator).validate(submission);
    }

    @ParameterizedTest(name = "[{index}] att={0}, fee={1}, fes={2}")
    @MethodSource("provideInvalidPreconditions")
    void validateWhenPreconditionsNotSatisfied(final boolean hasAttachments, final Boolean hasFee,
        final Boolean fesEnabled) {
        final String formType = BooleanUtils.isTrue(hasFee) ? FEE_FORM : NO_FEE_FORM;

        expectPreconditions(hasAttachments, hasFee, fesEnabled);
        expectErrorMessageDetails(formType);

        final SubmissionValidationException exception =
            assertThrows(SubmissionValidationException.class, () -> testValidator.validate(submission));

        assertThat(exception.getMessage(), is(MessageFormat
            .format("Attachments present in submission [{0}] for FES enabled form [{1}]", SUB_ID, formType)));
        verifyNoInteractions(nextValidator);
    }

    private void expectPreconditions(final boolean hasAttachments, final Boolean hasFee, final Boolean fesEnabled) {
        when(submission.getFormDetails()).thenReturn(formDetails);
        when(formDetails.getFileDetailsList())
            .thenReturn(hasAttachments ? Collections.singletonList(fileDetails) : Collections.emptyList());

        final String formType = BooleanUtils.isTrue(hasFee) ? FEE_FORM : NO_FEE_FORM;

        when(formDetails.getFormType()).thenReturn(formType);
        when(formRepository.findById(formType))
            .thenReturn(hasFee != null ? Optional.of(formTemplate) : Optional.empty());
        if (fesEnabled != null) {
            when(formTemplate.isFesEnabled()).thenReturn(fesEnabled);
        }
    }

    private void expectErrorMessageDetails(final String formType) {
        when(submission.getId()).thenReturn(SUB_ID);
        when(formTemplate.getFormType()).thenReturn(formType);
    }
}