package uk.gov.companieshouse.efs.api.util;

import java.time.temporal.Temporal;

/**
 * Implementations of TimestampGenerator are responsible for generating a timestamp.
 *
 * @param <T> The type of object that will be returned by the TimestampGenerator implementation.
 */
public interface TimestampGenerator<T extends Temporal> {

    /**
     * Generate a timestamp.
     *
     * @return A timestamp.
     */
    T generateTimestamp();
}
