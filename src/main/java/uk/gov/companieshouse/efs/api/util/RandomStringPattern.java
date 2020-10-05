package uk.gov.companieshouse.efs.api.util;

import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import org.apache.commons.lang.StringUtils;

/**
 * Class to generate different types of random strings using a template pattern.
 */
public final class RandomStringPattern {

    public static final int MIN_LENGTH = 1;
    public static final int MIN_SYMBOLS = 2;

    public static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static final String LOWER = UPPER.toLowerCase(Locale.ROOT);
    public static final String DIGITS = "0123456789";
    public static final String ALPHANUM = UPPER + DIGITS;
    public static final String ALPHANUMFULL = UPPER + LOWER + DIGITS;
    private final Random random;

    private final char meta;
    private final String pattern;
    private final char[] symbols;
    private final char[] buf;

    /**
     * The constructor.
     *
     * @param random used to generate random numbers
     * @param pattern the specified pattern
     * @param symbols the character set
     * @param meta the character to replace
     */
    public RandomStringPattern(Random random, String pattern, String symbols, char meta) {
        this.meta = meta;
        if (StringUtils.countMatches(pattern, Character.toString(meta)) < MIN_LENGTH) {
            throw new IllegalArgumentException("'" + meta + "' missing from 'pattern' parameter.");
        }
        if (symbols.length() < MIN_SYMBOLS) {
            throw new IllegalArgumentException("'symbols' parameter is too short.");
        }
        this.random = Objects.requireNonNull(random);
        this.pattern = Objects.requireNonNull(pattern);
        this.symbols = Objects.requireNonNull(symbols.toCharArray());
        this.buf = new char[pattern.length()];
    }

    /**
     * The constructor.
     *
     * @param random used to generate random numbers
     * @param pattern the specified pattern
     * @param symbols the character set
     */
    public RandomStringPattern(Random random, String pattern, String symbols) {
        this(random, pattern, symbols, '#');
    }

    /**
     * Generates a random string.
     *
     * @return the generated string
     */
    public String nextString() {
        for (int i = 0; i < buf.length; ++i) {
            char ch = (pattern == null) ? meta : pattern.charAt(i);
            buf[i] = (ch == meta) ? symbols[random.nextInt(symbols.length)] : ch;
        }
        return new String(buf);
    }

}
