package uk.gov.companieshouse.efs.api.events.service;

public interface TiffDownloadService {
    byte[] downloadTiffFile(String fileId);
}
