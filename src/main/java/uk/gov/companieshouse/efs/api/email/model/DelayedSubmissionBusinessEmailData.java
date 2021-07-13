package uk.gov.companieshouse.efs.api.email.model;

import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class DelayedSubmissionBusinessEmailData {

    private String to;
    private String subject;
    private List<DelayedSubmissionModel> submissions;
    private int thresholdInMinutes;

    /**
     * Constructor.
     *  @param to            dependency
     * @param subject       dependency
     * @param submissions   dependency
     * @param thresholdInMinutes   dependency
     */
    public DelayedSubmissionBusinessEmailData(String to, String subject, List<DelayedSubmissionModel> submissions, int thresholdInMinutes) {
        this.to = to;
        this.subject = subject;
        this.submissions = submissions;
        this.thresholdInMinutes = thresholdInMinutes;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public List<DelayedSubmissionModel> getSubmissions() {
        return submissions;
    }

    public void setSubmissions(List<DelayedSubmissionModel> submissions) {
        this.submissions = submissions;
    }

    public int getThresholdInMinutes() {
        return thresholdInMinutes;
    }

    public void setThresholdInMinutes(int thresholdInMinutes) {
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
        final DelayedSubmissionBusinessEmailData that = (DelayedSubmissionBusinessEmailData) o;
        return getThresholdInMinutes() == that.getThresholdInMinutes()
               && Objects.equals(getTo(), that.getTo())
               && Objects.equals(getSubject(), that.getSubject())
               && Objects.equals(getSubmissions(), that.getSubmissions());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTo(), getSubject(), getSubmissions(), getThresholdInMinutes());
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String to;
        private String subject;
        private List<DelayedSubmissionModel> submissions;
        private int thresholdInMinutes;

        public Builder withTo(String to) {
            this.to = to;
            return this;
        }

        public Builder withSubject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder withDelayedSubmissions(List<DelayedSubmissionModel> submissions) {
            this.submissions = submissions;
            return this;
        }

        public Builder withThresholdInMinutes(int thresholdInMinutes) {
            this.thresholdInMinutes = thresholdInMinutes;
            return this;
        }

        public DelayedSubmissionBusinessEmailData build() {
            return new DelayedSubmissionBusinessEmailData(to, subject, submissions, thresholdInMinutes);
        }
    }
}
