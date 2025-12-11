package uk.gov.companieshouse.efs.api.email.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import uk.gov.companieshouse.efs.api.submissions.model.Company;
import uk.gov.companieshouse.efs.api.submissions.model.Presenter;

public record InternalSubmissionEmailData(
    String to,
    String subject,
    @JsonProperty("confirmation_reference") String confirmationReference,
    @JsonProperty("presenter") Presenter presenter,
    @JsonProperty("company") Company company,
    @JsonProperty("form_type") String formType,
    @JsonProperty("email_file_details_list") List<EmailFileDetails> emailFileDetailsList
) {
    public InternalSubmissionEmailData {
        emailFileDetailsList = emailFileDetailsList == null ? List.of() : List.copyOf(emailFileDetailsList);
    }
}
