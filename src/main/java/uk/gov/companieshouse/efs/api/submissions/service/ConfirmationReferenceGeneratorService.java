package uk.gov.companieshouse.efs.api.submissions.service;

import java.security.SecureRandom;
import uk.gov.companieshouse.efs.api.util.IdentifierGeneratable;

/**
 * Generates a Confirmation Reference.
 */
public interface ConfirmationReferenceGeneratorService extends IdentifierGeneratable {
    void setSecureRandom(SecureRandom secureRandom);

    SecureRandom getSecureRandom();
}
