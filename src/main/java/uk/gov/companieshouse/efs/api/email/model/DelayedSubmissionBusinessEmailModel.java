package uk.gov.companieshouse.efs.api.email.model;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class DelayedSubmissionBusinessEmailModel {

    private List<DelayedSubmissionBusinessModel> delayedSubmissions;
    private String emailAddress;
    private int delayInMinutes;

    public DelayedSubmissionBusinessEmailModel(List<DelayedSubmissionBusinessModel> delayedSubmissions, String emailAddress,
        int delayInMinutes) {
        this.delayedSubmissions = delayedSubmissions;
        this.emailAddress = emailAddress;
        this.delayInMinutes = delayInMinutes;
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
        return Optional.ofNullable(delayedSubmissions).map(List::size).orElse(0);
    }

    public int getDelayInMinutes() {
        return delayInMinutes;
    }

    public void setDelayInMinutes(final int delayInMinutes) {
        this.delayInMinutes = delayInMinutes;
    }

    public int getDelayInHours() {
        return (int) TimeUnit.MINUTES.toHours(delayInMinutes);
    }

    public void setDelayInHours(int delayInHours) {
        this.delayInMinutes = (int) TimeUnit.HOURS.toMinutes(delayInHours);
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
        return getDelayInMinutes() == that.getDelayInMinutes() && Objects.equals(
            getDelayedSubmissions(), that.getDelayedSubmissions()) && Objects.equals(
            getEmailAddress(), that.getEmailAddress());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDelayedSubmissions(), getEmailAddress(), getDelayInMinutes());
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
