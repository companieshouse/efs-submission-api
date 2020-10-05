package uk.gov.companieshouse.efs.api.events.service;

import uk.gov.companieshouse.efs.api.events.service.model.FesLoaderModel;

public interface FesLoaderService {

    void insertSubmission(FesLoaderModel model);

}
