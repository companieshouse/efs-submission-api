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
public class SubmissionApiMapper {

    public Submission map(SubmissionApi submission) {
        return Submission.builder()
                .withId(submission.getId())
                .withConfirmationReference(submission.getConfirmationReference())
                .withCreatedAt(submission.getCreatedAt())
                .withSubmittedAt(submission.getSubmittedAt())
                .withLastModifiedAt(submission.getLastModifiedAt())
                .withCompany(mapCompany(submission.getCompany()))
                .withPresenter(mapPresenter(submission.getPresenter()))
                .withStatus(submission.getStatus())
                .withPaymentSessions(submission.getPaymentSessions())
                .withFormDetails(mapForm(submission.getSubmissionForm()))
                .withChipsRejectReasons(submission.getRejectReasons() == null ? null : mapRejectReasonList(submission.getRejectReasons()))
                .withConfirmAuthorised(submission.getConfirmAuthorised())
                .withFeeOnSubmission(submission.getFeeOnSubmission()).build();
    }

    private FormDetails mapForm(SubmissionFormApi submissionForm) {
        return Optional.ofNullable(submissionForm)
            .map(form -> new FormDetails(form.getBarcode(), form.getFormType(),
                mapFileDetailsList(form.getFileDetails())))
            .orElse(null);
    }

    private List<FileDetails> mapFileDetailsList(FileDetailListApi fileDetailList) {
        return fileDetailList.getList()
            .stream()
            .map(this::mapFileDetails)
            .collect(Collectors.toList());
    }

    private FileDetails mapFileDetails(final FileDetailApi details) {
        return Optional.ofNullable(details)
            .map(d -> new FileDetails(details.getFileId(), details.getFileName(),
                details.getFileSize(), details.getConvertedFileId(), details.getConversionStatus(),
                details.getNumberOfPages(), details.getLastModifiedAt()))
            .orElse(null);
    }

    private Company mapCompany(CompanyApi company) {
        return Optional.ofNullable(company)
            .map(theCompany -> new Company(theCompany.getCompanyNumber(),
                theCompany.getCompanyName()))
            .orElse(null);
    }

    private Presenter mapPresenter(PresenterApi presenter) {
        return Optional.ofNullable(presenter)
            .map(thePresenter -> new Presenter(thePresenter.getEmail()))
            .orElse(null);
    }

    private List<RejectReason> mapRejectReasonList(List<RejectReasonApi> rejectReasonList) {
        return rejectReasonList.stream()
            .map(rejectReasonApi -> new RejectReason(rejectReasonApi.getReason()))
            .collect(Collectors.toList());
    }
}
