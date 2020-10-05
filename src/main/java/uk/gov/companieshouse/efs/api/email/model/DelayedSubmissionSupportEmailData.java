package uk.gov.companieshouse.efs.api.email.model;

import java.util.List;
import java.util.Objects;

public class DelayedSubmissionSupportEmailData {
    private String to;
    private String subject;
    private List<DelayedSubmissionSupportModel> delayedSubmissions;

    public DelayedSubmissionSupportEmailData(String to, String subject, List<DelayedSubmissionSupportModel> delayedSubmissions) {
        this.to = to;
        this.subject = subject;
        this.delayedSubmissions = delayedSubmissions;
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

    public List<DelayedSubmissionSupportModel> getDelayedSubmissions() {
        return delayedSubmissions;
    }

    public void setDelayedSubmissions(List<DelayedSubmissionSupportModel> delayedSubmissions) {
        this.delayedSubmissions = delayedSubmissions;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final DelayedSubmissionSupportEmailData that = (DelayedSubmissionSupportEmailData) o;
        return Objects.equals(getTo(), that.getTo()) && Objects
            .equals(getSubject(), that.getSubject()) && Objects
                   .equals(getDelayedSubmissions(), that.getDelayedSubmissions());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTo(), getSubject(), getDelayedSubmissions());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String to;
        private String subject;
        private List<DelayedSubmissionSupportModel> delayedSubmissions;


        public Builder withTo(String to) {
            this.to = to;
            return this;
        }

        public Builder withSubject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder withDelayedSubmissions(List<DelayedSubmissionSupportModel> delayedSubmissions) {
            this.delayedSubmissions = delayedSubmissions;
            return this;
        }

        public DelayedSubmissionSupportEmailData build() {
            return new DelayedSubmissionSupportEmailData(to, subject, delayedSubmissions);
        }
    }
}
