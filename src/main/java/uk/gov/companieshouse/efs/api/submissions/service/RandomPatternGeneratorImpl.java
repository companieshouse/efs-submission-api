package uk.gov.companieshouse.efs.api.submissions.service;

import java.security.SecureRandom;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.efs.api.util.RandomStringPattern;

/**
 * Produce a random string based on a pattern and a symbol set.
 */
@Service
public class RandomPatternGeneratorImpl implements ConfirmationReferenceGeneratorService {
    public static final int BASE64_LEN = 64;

    private SecureRandom secureRandom;

    private String pattern;
    private String symbolSet;

    /**
     * Generates random strings based on a specified pattern and symbol set.
     * <p>
     * Each '#' character in the pattern is replaced with a random character from the provided symbol set. The
     * randomness is supplied by a cryptographically secure {@link SecureRandom} instance.
     * </p>
     *
     * <p>
     * Paired with a carefully chosen symbol set such as zbase32,  it helps customers by producing identifier strings
     * that
     * <ol>
     *     <li>avoid ambiguous symbols (0/O, 1/l/L, 8/B), reducing errors reading, writing and speaking them</li>
     *     <li>avoid case errors because all symbols are lower case</li>
     *     <li>are easy to distinguish on most keyboards and fonts</li>
     * </ol>
     * Using a pattern with separator characters to break the identifier string into parts assists with readability,
     * error detection, and easier manual entry.
     * </p>
     *
     * <p>
     * Example usage:
     * <pre>
     *   RandomPatternGeneratorImpl generator = new RandomPatternGeneratorImpl(
     *       new SecureRandom(), "##-####", "abcdefghijkmnopqrstuwxyz13456789");
     *   String randomId = generator.generateId(); // e.g., "yk-3d7h"
     * </pre>
     * </p>
     *
     * @param secureRandom a crypto-strong PRNG
     * @param pattern      String containing '#' chars as symbol placeholders and other chars as literals
     * @param symbolSet    String containing set of unique chars to be picked from at random
     */
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
        // to properly initialize the PRNG it needs to be used once, so we generate some random bytes and then clear them from memory
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
