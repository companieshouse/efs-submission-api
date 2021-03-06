package uk.gov.companieshouse.efs.api.submissions.service;

import uk.gov.companieshouse.api.model.efs.submissions.CompanyApi;
import uk.gov.companieshouse.api.model.efs.submissions.FileListApi;
import uk.gov.companieshouse.api.model.efs.submissions.FormTypeApi;
import uk.gov.companieshouse.api.model.efs.submissions.PresenterApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionResponseApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus;
import uk.gov.companieshouse.api.model.paymentsession.SessionListApi;
import uk.gov.companieshouse.efs.api.payment.PaymentClose;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.submissions.validator.exception.SubmissionValidationException;

public interface SubmissionService {

    SubmissionApi readSubmission(String id);

    SubmissionResponseApi createSubmission(PresenterApi presenterApi);

    SubmissionResponseApi updateSubmissionWithCompany(String id, CompanyApi companyApi);

    SubmissionResponseApi updateSubmissionWithForm(String id, FormTypeApi formApi);

    SubmissionResponseApi updateSubmissionWithFileDetails(String id, FileListApi fileListApi);

    SubmissionResponseApi updateSubmissionWithPaymentSessions(String id, SessionListApi paymentSessions);

    SubmissionResponseApi updateSubmissionWithPaymentOutcome(String id, PaymentClose paymentClose);

    SubmissionResponseApi completeSubmission(String id) throws SubmissionValidationException;

    SubmissionResponseApi updateSubmissionQueued(Submission submission);

    SubmissionResponseApi updateSubmissionBarcode(String id, String barcode);

    SubmissionResponseApi updateSubmissionStatus(String id, SubmissionStatus status);

    SubmissionResponseApi updateSubmissionConfirmAuthorised(String id, Boolean confirmAuthorised);

    void updateSubmission(Submission submission);

}
