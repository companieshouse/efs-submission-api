package uk.gov.companieshouse.efs.api.paymentreports.model;

import java.time.LocalDateTime;

public class PaymentTransactionBuilder {
    private String submissionId;
    private String customerRef;
    private String userEmail;
    private String submittedAt;
    private String amountPaid;
    private String paymentRef;
    private String formType;
    private String companyNumber;

    public PaymentTransactionBuilder withSubmissionId(final String submissionId) {
        this.submissionId = submissionId;
        return this;
    }

    public PaymentTransactionBuilder withCustomerRef(final String customerRef) {
        this.customerRef = customerRef;
        return this;
    }

    public PaymentTransactionBuilder withUserEmail(final String userEmail) {
        this.userEmail = userEmail;
        return this;
    }

    public PaymentTransactionBuilder withSubmittedAt(final LocalDateTime submittedAt) {
        this.submittedAt = submittedAt.toString();
        return this;
    }

    public PaymentTransactionBuilder withAmountPaid(final String amountPaid) {
        this.amountPaid = amountPaid;
        return this;
    }

    public PaymentTransactionBuilder withPaymentRef(final String paymentRef) {
        this.paymentRef = paymentRef;
        return this;
    }

    public PaymentTransactionBuilder withFormType(final String formType) {
        this.formType = formType;
        return this;
    }

    public PaymentTransactionBuilder withCompanyNumber(final String companyNumber) {
        this.companyNumber = companyNumber;
        return this;
    }

    public PaymentTransaction build() {
        PaymentTransaction tr = new PaymentTransaction();

        tr.setSubmissionId(submissionId);
        tr.setCustomerRef(customerRef);
        tr.setUserEmail(userEmail);
        tr.setSubmittedAt(submittedAt);
        tr.setAmountPaid(amountPaid);
        tr.setPaymentRef(paymentRef);
        tr.setFormType(formType);
        tr.setCompanyNumber(companyNumber);

        return tr;
    }
}