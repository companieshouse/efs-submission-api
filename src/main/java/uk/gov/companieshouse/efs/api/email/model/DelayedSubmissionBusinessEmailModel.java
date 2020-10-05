package uk.gov.companieshouse.efs.api.email.model;

import java.util.List;
import java.util.Objects;

public class DelayedSubmissionBusinessEmailModel {

    private List<DelayedSubmissionBusinessModel> delayedSubmissions;
    private String emailAddress;
    private int delayInHours;

    public DelayedSubmissionBusinessEmailModel(List<DelayedSubmissionBusinessModel> delayedSubmissions, String emailAddress, int delayInHours) {
        this.delayedSubmissions = delayedSubmissions;
        this.emailAddress = emailAddress;
        this.delayInHours = delayInHours;
    }

    public List<DelayedSubmissionBusinessModel> getDelayedSubmissions() {
        return delayedSubmissions;
    }

    public void setDelayedSubmissions(List<DelayedSubmissionBusinessModel> delayedSubmissions) {
        this.delayedSubmissions = delayedSubmissions;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public int getNumberOfDelayedSubmissions() {
        return this.delayedSubmissions.size();
    }

    public int getDelayInHours() {
        return delayInHours;
    }

    public void setDelayInHours(int delayInHours) {
        this.delayInHours = delayInHours;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final DelayedSubmissionBusinessEmailModel that = (DelayedSubmissionBusinessEmailModel) o;
        return getDelayInHours() == that.getDelayInHours() && Objects
            .equals(getDelayedSubmissions(), that.getDelayedSubmissions()) && Objects
                   .equals(getEmailAddress(), that.getEmailAddress());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDelayedSubmissions(), getEmailAddress(), getDelayInHours());
    }
}
