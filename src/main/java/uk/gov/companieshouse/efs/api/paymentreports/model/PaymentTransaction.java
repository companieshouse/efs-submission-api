package uk.gov.companieshouse.efs.api.paymentreports.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.Objects;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@JsonPropertyOrder
public class PaymentTransaction {

    private String submissionId;
    private String customerRef;
    private String userEmail;
    private String submittedAt;
    private String amountPaid;
    private String paymentRef;
    private String formType;
    private String companyNumber;

    public PaymentTransaction() {
        // intentionally blank
    }

    public String getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(final String submissionId) {
        this.submissionId = submissionId;
    }

    public String getCustomerRef() {
        return customerRef;
    }

    public void setCustomerRef(final String customerRef) {
        this.customerRef = customerRef;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(final String userEmail) {
        this.userEmail = userEmail;
    }

    public String getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(final String submittedAt) {
        this.submittedAt = submittedAt;
    }

    public String getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(final String amountPaid) {
        this.amountPaid = amountPaid;
    }

    public String getPaymentRef() {
        return paymentRef;
    }

    public void setPaymentRef(final String paymentRef) {
        this.paymentRef = paymentRef;
    }

    public String getFormType() {
        return formType;
    }

    public void setFormType(final String formType) {
        this.formType = formType;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(final String companyNumber) {
        this.companyNumber = companyNumber;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final PaymentTransaction that = (PaymentTransaction) o;
        return Objects.equals(getSubmissionId(), that.getSubmissionId()) && Objects
            .equals(getCustomerRef(), that.getCustomerRef()) && Objects.equals(getUserEmail(), that.getUserEmail())
            && Objects.equals(getSubmittedAt(), that.getSubmittedAt()) && Objects
            .equals(getAmountPaid(), that.getAmountPaid()) && Objects.equals(getPaymentRef(), that.getPaymentRef())
            && Objects.equals(getFormType(), that.getFormType()) && Objects
            .equals(getCompanyNumber(), that.getCompanyNumber());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSubmissionId(), getCustomerRef(), getUserEmail(), getSubmittedAt(), getAmountPaid(),
            getPaymentRef(), getFormType(), getCompanyNumber());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("submissionId", submissionId)
            .append("customerRef", customerRef).append("userEmail", userEmail).append("submittedAt", submittedAt)
            .append("amountPaid", amountPaid).append("paymentRef", paymentRef).append("formType", formType)
            .append("companyNumber", companyNumber).toString();
    }
}
