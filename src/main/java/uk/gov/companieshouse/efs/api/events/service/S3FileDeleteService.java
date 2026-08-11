package uk.gov.companieshouse.efs.api.events.service;

public interface S3FileDeleteService {
    /**
     * Deletes a file from S3 storage.
     *
     * @param submissionId the ID of the submission the file belongs to, used for logging context
     * @param fileId  the S3 fileId of the file to delete
     */
    void deleteFile(final String submissionId, String fileId);
}
