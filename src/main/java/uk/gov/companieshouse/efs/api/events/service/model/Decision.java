package uk.gov.companieshouse.efs.api.events.service.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;

public class Decision {

    private Submission submission;
    private DecisionResult decisionResult;
    private boolean changed;
    private int numberOfDecisions;
    private List<String> infectedFiles;

    public Decision(Submission submission) {
        this.submission = submission;
        this.infectedFiles = new ArrayList<>();
    }

    public Submission getSubmission() {
        return submission;
    }

    public void setSubmission(Submission submission) {
        this.submission = submission;
    }

    public DecisionResult getDecisionResult() {
        return decisionResult;
    }

    public void setDecisionResult(DecisionResult decisionResult) {
        this.decisionResult = decisionResult;
    }

    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    public int getExpectedDecisions() {
        return submission.getFormDetails().getFileDetailsList().size();
    }

    public int getNumberOfDecisions() {
        return numberOfDecisions;
    }

    public void incrementNumberOfDecisions() {
        numberOfDecisions++;
    }

    public boolean containsInfectedFile() {
        return !this.infectedFiles.isEmpty();
    }

    public void addInfectedFile(String filename) {
        this.infectedFiles.add(filename);
    }

    public List<String> getInfectedFiles() {
        return Collections.unmodifiableList(this.infectedFiles);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Decision)) {
            return false;
        }
        Decision decision1 = (Decision) o;
        return isChanged() == decision1.isChanged()
               && getExpectedDecisions() == decision1.getExpectedDecisions()
               && getNumberOfDecisions() == decision1.getNumberOfDecisions()
               && Objects.equals(getSubmission(), decision1.getSubmission())
               && getDecisionResult() == decision1.getDecisionResult()
               && Objects.equals(getInfectedFiles(), decision1.getInfectedFiles());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSubmission(), getDecisionResult(), isChanged(), getExpectedDecisions(), getNumberOfDecisions(), getInfectedFiles());
    }
}
