package uk.gov.companieshouse.efs.api.exception;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Error class for api validation errors.
 */
public class ApiError {
    @JsonProperty
    private String error;
    @JsonProperty("error_values") // must match uk.gov.companieshouse.api.error.ApiError @Key
    private Map<String, String> errorValues;
    @JsonProperty
    private String location;
    @JsonProperty("location_type") // must match uk.gov.companieshouse.api.error.ApiError @Key
    private String locationType;
    @JsonProperty
    private String type;

    public ApiError() {
    }

    /**
     * Constructor for APIError.
     *
     * @param error the error message
     * @param location location of the error
     * @param locationType error location type
     * @param type validation error type
     */
    @JsonCreator
    public ApiError(String error, String location, String locationType, String type) {
        Objects.requireNonNull(error, "'error' cannot be null");
        Objects.requireNonNull(type, "'type' cannot be null");
        this.error = error;
        this.location = location;
        this.locationType = locationType;
        this.type = type;
    }

    /**
     * constructor for APIError.
     *
     * @param other an APIError
     */
    public ApiError(ApiError other) {
        this(other.getError(), other.getLocation(), other.getLocationType(), other.getType());
        this.errorValues = other.getErrorValues();
    }

    public String getError() {
        return this.error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Map<String, String> getErrorValues() {
        return this.errorValues == null ? Collections.emptyMap() : Collections.unmodifiableMap(this.errorValues);
    }

    public void setErrorValues(Map<String, String> errorValues) {
        this.errorValues = new HashMap<>(errorValues);
    }

    /**
     * Adds information about the error to the error map.
     *
     * @param argument description of the error
     * @param value the error value
     */
    public void addErrorValue(String argument, String value) {
        Objects.requireNonNull(argument, "'argument' cannot be null or empty");
        Objects.requireNonNull(value, "'value' cannot be null or empty");
        if (this.errorValues == null) {
            this.errorValues = new HashMap<>();
        }
        this.errorValues.put(argument, value);
    }

    public String getLocation() {
        return this.location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLocationType() {
        return this.locationType;
    }

    public void setLocationType(String locationType) {
        this.locationType = locationType;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ApiError apiError = (ApiError) o;
        return Objects.equals(getError(), apiError.getError()) && Objects.equals(getErrorValues(),
            apiError.getErrorValues()) && Objects.equals(getLocation(), apiError.getLocation()) && Objects.equals(
            getLocationType(), apiError.getLocationType()) && Objects.equals(getType(), apiError.getType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getError(), getErrorValues(), getLocation(), getLocationType(), getType());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("error", error).append("errorValues",
            errorValues).append("location", location).append("locationType", locationType).append("type", type)
            .toString();
    }
}
