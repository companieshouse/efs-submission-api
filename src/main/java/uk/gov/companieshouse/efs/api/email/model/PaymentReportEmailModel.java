package uk.gov.companieshouse.efs.api.email.model;

import java.util.Objects;

public class PaymentReportEmailModel {

    private String fileLink;
    private String fileName;
    private boolean hasNoPaymentTransactions;

    /**
     * Constructor.
     *
     * @param fileLink                  dependency
     * @param fileName                  dependency
     * @param hasNoPaymentTransactions  dependency
     */
    public PaymentReportEmailModel(final String fileLink, final String fileName, final boolean hasNoPaymentTransactions) {
        this.fileLink = fileLink;
        this.fileName = fileName;
        this.hasNoPaymentTransactions = hasNoPaymentTransactions;
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
        final PaymentReportEmailModel that = (PaymentReportEmailModel) o;
        return Objects.equals(getFileLink(), that.getFileLink())
                && Objects.equals(getFileName(), that.getFileName())
                && Objects.equals(getHasNoPaymentTransactions(), that.getHasNoPaymentTransactions());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFileLink(), getFileName(), getHasNoPaymentTransactions());
    }
}