package uk.gov.companieshouse.efs.api.companyauthallowlist.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.efs.api.companyauthallowlist.repository.CompanyAuthAllowListRepository;
import uk.gov.companieshouse.efs.api.config.Config;

/**
 * Stores and retrieves the company authentication allow list information.
 */
@Service
@Import(Config.class)
public class CompanyAuthAllowListServiceImpl implements CompanyAuthAllowListService {

    private CompanyAuthAllowListRepository repository;

    /**
     * CompanyAuthenticationAllowListServiceImpl constructor.
     *
     * @param repository the {@link CompanyAuthAllowListRepository}
     */
    @Autowired
    public CompanyAuthAllowListServiceImpl(final CompanyAuthAllowListRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean isOnAllowList(final String emailAddress) {
        return repository.existsByEmailAddressIgnoreCase(emailAddress);
    }
}
