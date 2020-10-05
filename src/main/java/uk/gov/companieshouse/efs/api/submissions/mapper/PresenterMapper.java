package uk.gov.companieshouse.efs.api.submissions.mapper;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.efs.submissions.PresenterApi;
import uk.gov.companieshouse.efs.api.submissions.model.Presenter;

@Component
public class PresenterMapper {
    public Presenter map(PresenterApi presenterApi) {
        return new Presenter(presenterApi.getEmail());
    }
}
