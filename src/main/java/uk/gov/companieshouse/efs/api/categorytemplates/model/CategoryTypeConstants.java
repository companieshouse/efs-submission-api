package uk.gov.companieshouse.efs.api.categorytemplates.model;

import java.util.EnumSet;
import java.util.Optional;

public enum CategoryTypeConstants {
    ROOT(""),
    CHANGE_OF_CONSTITUTION("CC"),
    INSOLVENCY("INS"),
    OTHER("*"),
    SCOTTISH_LIMITED_PARTNERSHIP("SLP"),
    SCOTTISH_QUALIFYING_PARTNERSHIP("SQP"),
    SHARE_CAPITAL("SH"),
    SHARE_CAPITAL_REDUCTION("SH-RED"),
    REGISTRAR_POWERS("RP");

    CategoryTypeConstants(final String value) {
        this.value = value;
    }

    private final String value;

    public String getValue() {
        return value;
    }

    public static Optional<CategoryTypeConstants> nameOf(final String value) {
        return EnumSet.allOf(CategoryTypeConstants.class).stream().filter(v -> v.getValue().equals(value)).findAny();
    }
}
