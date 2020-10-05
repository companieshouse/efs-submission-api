package uk.gov.companieshouse.efs.api.paymentreports.mapper;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.efs.api.paymentreports.model.PaymentTransaction;
import uk.gov.companieshouse.efs.api.paymentreports.model.PaymentTransactionBuilder;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;

@Component
public class PaymentReportMapper {
    public PaymentTransaction map(final Submission submission) {
        return new PaymentTransactionBuilder().withSubmissionId(submission.getId())
            .withCustomerRef(submission.getConfirmationReference()).withUserEmail(submission.getPresenter().getEmail())
            .withSubmittedAt(submission.getSubmittedAt()).withAmountPaid(submission.getFeeOnSubmission())
            .withPaymentRef(submission.getPaymentReference()).withFormType(submission.getFormDetails().getFormType())
            .withCompanyNumber(submission.getCompany().getCompanyNumber()).build();
    }
}