package uk.gov.companieshouse.efs.api.email.model;

import java.util.Objects;
import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class PaymentReportEmailData {

    private String to;
    private String subject;
    private String fileLink;
    private String fileName;
    private boolean hasNoPaymentTransactions;

    public PaymentReportEmailData(final String to, final String subject, final String fileLink, final String fileName,
                                    final boolean hasNoPaymentTransactions) {
        this.to = to;
        this.subject = subject;
        this.fileLink = fileLink;
        this.fileName = fileName;
        this.hasNoPaymentTransactions = hasNoPaymentTransactions;
    }

    public String getTo() {
        return to;
    }

    public void setTo(final String to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(final String subject) {
        this.subject = subject;
    }

    public String getFileLink() {
        return fileLink;
    }

    public void setFileLink(final String fileLink) {
        this.fileLink = fileLink;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    public boolean getHasNoPaymentTransactions() {
        return hasNoPaymentTransactions;
    }

    public void setHasNoPaymentTransactions(boolean hasNoPaymentTransactions) {
        this.hasNoPaymentTransactions = hasNoPaymentTransactions;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final PaymentReportEmailData that = (PaymentReportEmailData) o;
        return Objects.equals(getTo(), that.getTo()) && Objects.equals(getSubject(), that.getSubject()) && Objects
            .equals(getFileLink(), that.getFileLink()) && Objects.equals(getFileName(), that.getFileName())
                && Objects.equals(getHasNoPaymentTransactions(), that.getHasNoPaymentTransactions());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTo(), getSubject(), getFileLink(), getFileName(), getHasNoPaymentTransactions());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, RecursiveToStringStyle.SHORT_PREFIX_STYLE).append("to", to)
                .append("subject", subject).append("fileLink", fileLink).append("fileName", fileName)
                .append("hasNoPaymentTransactions", hasNoPaymentTransactions).toString();
    }

    public static PaymentReportEmailData.Builder builder() {
        return new PaymentReportEmailData.Builder();
    }

    public static class Builder {

        private String to;
        private String subject;
        private String fileLink;
        private String fileName;
        private boolean hasNoPaymentTransactions;

        public Builder withTo(String to) {
            this.to = to;
            return this;
        }

        public Builder withSubject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder withFileLink(String fileLink) {
            this.fileLink = fileLink;
            return this;
        }

        public Builder withFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder witHasNoPaymentTransactions(boolean hasNoPaymentTransactions) {
            this.hasNoPaymentTransactions = hasNoPaymentTransactions;
            return this;
        }

        public PaymentReportEmailData build() {
            return new PaymentReportEmailData(to, subject, fileLink, fileName, hasNoPaymentTransactions);
        }
    }
}