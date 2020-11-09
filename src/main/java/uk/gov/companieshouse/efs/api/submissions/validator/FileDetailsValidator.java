package uk.gov.companieshouse.efs.api.submissions.validator;

import java.util.List;
import uk.gov.companieshouse.efs.api.submissions.model.FileDetails;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.submissions.validator.exception.SubmissionValidationException;

public class FileDetailsValidator extends ValidatorImpl<Submission> implements Validator<Submission> {
    @Override
    public void validate(final Submission input) throws SubmissionValidationException {
        final List<FileDetails> details = input.getFormDetails().getFileDetailsList();

        if (details == null) {
            throw new SubmissionValidationException(
                String.format("File details are absent in submission [%s]", input.getId()));
        } else if (details.isEmpty()) {
            throw new SubmissionValidationException(
                String.format("File details are empty in submission [%s]", input.getId()));
        } else if (details.contains(null)) {
            throw new SubmissionValidationException(
                String.format("File details contains null in submission [%s]", input.getId()));
        }
        super.validate(input);
    }
}
