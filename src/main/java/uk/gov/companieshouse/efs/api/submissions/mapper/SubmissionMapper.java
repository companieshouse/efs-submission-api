package uk.gov.companieshouse.efs.api.submissions.mapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.efs.submissions.CompanyApi;
import uk.gov.companieshouse.api.model.efs.submissions.FileDetailApi;
import uk.gov.companieshouse.api.model.efs.submissions.FileDetailListApi;
import uk.gov.companieshouse.api.model.efs.submissions.PresenterApi;
import uk.gov.companieshouse.api.model.efs.submissions.RejectReasonApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionFormApi;
import uk.gov.companieshouse.efs.api.submissions.model.Company;
import uk.gov.companieshouse.efs.api.submissions.model.FileDetails;
import uk.gov.companieshouse.efs.api.submissions.model.FormDetails;
import uk.gov.companieshouse.efs.api.submissions.model.Presenter;
import uk.gov.companieshouse.efs.api.submissions.model.RejectReason;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;

@Component
public class SubmissionMapper {

    public SubmissionApi map(Submission submission) {
        return new SubmissionApi(
                submission.getId(),
                submission.getConfirmationReference(),
                mapPresenter(submission.getPresenter()),
                mapCompany(submission.getCompany()),
                submission.getStatus(),
                submission.getPaymentSessions(),
                submission.getFeeOnSubmission(),
                submission.getConfirmAuthorised(),
                mapForm(submission.getFormDetails()),
                submission.getCreatedAt(),
                submission.getSubmittedAt(),
                submission.getLastModifiedAt(),
                mapRejectReasons(submission.getChipsRejectReasons())
        );
    }

    private SubmissionFormApi mapForm(FormDetails formDetails) {
        return Optional.ofNullable(formDetails)
                .map(theFormDetails -> new SubmissionFormApi(theFormDetails.getBarcode(), theFormDetails.getFormType(), mapFileDetails(theFormDetails.getFileDetailsList())))
                .orElse(null);
    }

    private FileDetailListApi mapFileDetails(List<FileDetails> fileDetailsList) {
        return Optional.ofNullable(fileDetailsList).map(theFileDetailsList -> theFileDetailsList.stream()
                .map(fileDetails -> new FileDetailApi(
                        fileDetails.getFileId(),
                        fileDetails.getFileName(),
                        fileDetails.getFileSize(),
                        fileDetails.getConvertedFileId(),
                        fileDetails.getConversionStatus(),
                        fileDetails.getNumberOfPages(),
                        fileDetails.getLastModifiedAt())).collect(Collectors.toCollection(FileDetailListApi::new)))
                .orElse(null);
    }

    private CompanyApi mapCompany(Company company) {
        return Optional.ofNullable(company)
                .map(theCompany -> new CompanyApi(theCompany.getCompanyNumber(), theCompany.getCompanyName()))
                .orElse(null);
    }

    private PresenterApi mapPresenter(Presenter presenter) {
        return Optional.ofNullable(presenter)
                .map(thePresenter -> new PresenterApi(thePresenter.getEmail()))
                .orElse(null);
    }

    private List<RejectReasonApi> mapRejectReasons(List<RejectReason> rejectReasons) {
        return Optional.ofNullable(rejectReasons)
                .map(theRejectReasons -> theRejectReasons.stream()
                        .map(rejectReason -> new RejectReasonApi(rejectReason.getReason())).toList())
                .orElse(null);
    }
}
