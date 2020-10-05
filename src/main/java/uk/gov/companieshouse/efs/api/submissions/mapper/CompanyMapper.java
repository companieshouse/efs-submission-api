package uk.gov.companieshouse.efs.api.submissions.mapper;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.efs.submissions.CompanyApi;
import uk.gov.companieshouse.efs.api.submissions.model.Company;

@Component
public class CompanyMapper {
    public Company map(CompanyApi companyApi) {
        return new Company(companyApi.getCompanyNumber(), companyApi.getCompanyName());
    }
}
