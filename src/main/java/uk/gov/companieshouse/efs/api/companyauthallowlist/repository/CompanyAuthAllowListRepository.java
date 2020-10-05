package uk.gov.companieshouse.efs.api.companyauthallowlist.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.efs.api.companyauthallowlist.model.CompanyAuthAllowListEntry;

/**
 * Retrieve company authentication allow list information.
 */
@Repository
public interface CompanyAuthAllowListRepository extends MongoRepository<CompanyAuthAllowListEntry, String> {

    boolean existsByEmailAddressIgnoreCase(String emailAddress);

}