package uk.gov.companieshouse.efs.api.email.model;

import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class DelayedSubmissionSupportEmailModel {
    private List<DelayedSubmissionSupportModel> delayedSubmissions;

    public DelayedSubmissionSupportEmailModel(List<DelayedSubmissionSupportModel> delayedSubmissions) {
        this.delayedSubmissions = delayedSubmissions;
    }

    public List<DelayedSubmissionSupportModel> getDelayedSubmissions() {
        return delayedSubmissions;
    }

    public void setDelayedSubmissions(List<DelayedSubmissionSupportModel> delayedSubmissions) {
        this.delayedSubmissions = delayedSubmissions;
    }

    public int getNumberOfDelayedSubmissions() {
        return delayedSubmissions.size();
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
        return Objects.equals(getDelayedSubmissions(), that.getDelayedSubmissions());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDelayedSubmissions());
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
