package uk.gov.companieshouse.efs.api.events.service;

import java.util.List;
import uk.gov.companieshouse.efs.api.events.service.model.Decision;


public interface MessageService {
    void queueMessages(List<Decision> submissions);
}
