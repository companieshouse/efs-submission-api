package uk.gov.companieshouse.efs.api.email.model;

import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class DelayedSubmissionSupportEmailModel {
    private List<DelayedSubmissionModel> delayedSubmissions;
    private int thresholdInMinutes;

    public DelayedSubmissionSupportEmailModel(
        List<DelayedSubmissionModel> delayedSubmissions, final int thresholdInMinutes) {
        this.delayedSubmissions = delayedSubmissions;
        this.thresholdInMinutes = thresholdInMinutes;
    }

    public List<DelayedSubmissionModel> getDelayedSubmissions() {
        return delayedSubmissions;
    }

    public void setDelayedSubmissions(List<DelayedSubmissionModel> delayedSubmissions) {
        this.delayedSubmissions = delayedSubmissions;
    }

    public int getNumberOfDelayedSubmissions() {
        return delayedSubmissions.size();
    }

    public int getThresholdInMinutes() {
        return thresholdInMinutes;
    }

    public void setThresholdInMinutes(final int thresholdInMinutes) {
        this.thresholdInMinutes = thresholdInMinutes;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final DelayedSubmissionSupportEmailModel that = (DelayedSubmissionSupportEmailModel) o;
        return getThresholdInMinutes() == that.getThresholdInMinutes() && Objects.equals(
            getDelayedSubmissions(), that.getDelayedSubmissions());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDelayedSubmissions(), getThresholdInMinutes());
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
