package uk.gov.companieshouse.efs.api.events.service;

public interface S3FileDeleteService {
    /**
     * Asynchronously deletes a file from S3 storage.
     *
     * <p>Implementations must return immediately; the actual S3 request runs on
     * a background thread. Delete failures should be logged without rethrowing.</p>
     *
     * @param submissionId the ID of the submission the file belongs to, used for logging context
     * @param fileId  the S3 fileId of the file to delete
     */
    void deleteFile(final String submissionId, String fileId);
}
