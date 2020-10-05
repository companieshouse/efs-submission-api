package uk.gov.companieshouse.efs.api.companyauthallowlist.service;

public interface CompanyAuthAllowListService {

    /**
     * Retrieves if an email exists within the collection (case insensitive matching).
     *
     * @return true if email address exists in the collection, false otherwise.
     */
    default boolean isOnAllowList(String emailAddress) {
        throw new UnsupportedOperationException("not implemented");
    }
}