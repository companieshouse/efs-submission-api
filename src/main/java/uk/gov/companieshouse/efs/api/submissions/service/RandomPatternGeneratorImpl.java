package uk.gov.companieshouse.efs.api.submissions.service;

import java.security.SecureRandom;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.efs.api.util.RandomStringPattern;

/**
 * Produce a random string based on a pattern.
 */
@Service
public class RandomPatternGeneratorImpl implements ConfirmationReferenceGeneratorService {
    public static final int BASE64_LEN = 64;

    private SecureRandom secureRandom;

    private String pattern;
    private String symbolSet;

    /**
     * Produce a random string based on a pattern. Each '#' character in the pattern will be
     * substituted with a random character from the list of symbolSet in symbolSet1.
     *
     * @param secureRandom a crypto-strong PRNG
     * @param pattern      String pattern containing '#' characters.
     * @param symbolSet    String containing set of symbolSet to be chosen at random from symbolSet
     */
    @Autowired
    public RandomPatternGeneratorImpl(final SecureRandom secureRandom, @Qualifier("pattern") final String pattern,
        @Qualifier("refSymbolSet") final String symbolSet) {
        this.pattern = pattern;
        this.symbolSet = symbolSet;
        this.secureRandom = secureRandom;
        secureInit();
    }

    @Override
    public final void setSecureRandom(final SecureRandom secureRandom) {
        this.secureRandom = secureRandom;
        secureInit();
    }

    private void secureInit() {
        // to properly initialize the PRNG it needs to be used once
        if (secureRandom != null) {
            final byte[] ar = new byte[BASE64_LEN];
            secureRandom.nextBytes(ar);
            Arrays.fill(ar, (byte) 0);
        }
    }

    @Override
    public String generateId() {
        return new RandomStringPattern(secureRandom, pattern, symbolSet).nextString();
    }

    public SecureRandom getSecureRandom() {
        return secureRandom;
    }

    public String getPattern() {
        return pattern;
    }

    public String getSymbolSet() {
        return symbolSet;
    }
}
