package uk.gov.companieshouse.efs.api.email.model;

import java.util.List;
import java.util.Objects;

public class DelayedSubmissionBusinessEmailData {

    private String to;
    private String subject;
    private List<DelayedSubmissionBusinessModel> submissions;
    private long delayInDays;

    /**
     * Constructor.
     *
     * @param to            dependency
     * @param subject       dependency
     * @param submissions   dependency
     * @param delayInDays   dependency
     */
    public DelayedSubmissionBusinessEmailData(String to, String subject, List<DelayedSubmissionBusinessModel> submissions, long delayInDays) {
        this.to = to;
        this.subject = subject;
        this.submissions = submissions;
        this.delayInDays = delayInDays;
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

    public List<DelayedSubmissionBusinessModel> getSubmissions() {
        return submissions;
    }

    public void setSubmissions(List<DelayedSubmissionBusinessModel> submissions) {
        this.submissions = submissions;
    }

    public long getDelayInDays() {
        return delayInDays;
    }

    public void setDelayInDays(long delayInDays) {
        this.delayInDays = delayInDays;
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
        return getDelayInDays() == that.getDelayInDays()
               && Objects.equals(getTo(), that.getTo())
               && Objects.equals(getSubject(), that.getSubject())
               && Objects.equals(getSubmissions(), that.getSubmissions());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTo(), getSubject(), getSubmissions(), getDelayInDays());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String to;
        private String subject;
        private List<DelayedSubmissionBusinessModel> submissions;
        private long delayInDays;

        public Builder withTo(String to) {
            this.to = to;
            return this;
        }

        public Builder withSubject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder withDelayedSubmissions(List<DelayedSubmissionBusinessModel> submissions) {
            this.submissions = submissions;
            return this;
        }

        public Builder withDelayInDays(long delayInDays) {
            this.delayInDays = delayInDays;
            return this;
        }

        public DelayedSubmissionBusinessEmailData build() {
            return new DelayedSubmissionBusinessEmailData(to, subject, submissions, delayInDays);
        }
    }
}
