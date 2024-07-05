package uk.gov.companieshouse.efs.api.submissions.mapper;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.efs.api.submissions.model.Presenter;
import uk.gov.companieshouse.efs.api.submissions.model.PresenterApi;

@Component
public class PresenterMapper {
    public Presenter map(PresenterApi presenterApi) {
        return new Presenter(presenterApi.getEmail());
    }
}
