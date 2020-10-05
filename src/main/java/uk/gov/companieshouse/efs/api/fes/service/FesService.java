package uk.gov.companieshouse.efs.api.fes.service;

import uk.gov.companieshouse.api.model.efs.fes.FesSubmissionStatus;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;

public interface FesService {

    void updateSubmissionStatusByBarcode(String barcode, FesSubmissionStatus status);

    Submission getSubmissionForBarcode(String barcode);

}
