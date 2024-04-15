package it.pagopa.ecommerce.commons.domain;

import java.util.Objects;

/**
 * Domain class representing a credit/debit card BIN (bank identification
 * number)
 *
 * @param value the card bin
 */
public record BIN(String value) {
    /**
     * BIN format used to check for valid values
     */
    public static final String BIN_FORMAT = "\\d{4}\\d?\\d?";

    /**
     * Primary constructor
     *
     * @param value the card BIN
     */
    public BIN {
        Objects.requireNonNull(value);

        /*
         * A card BIN can be of length 4, 5, or 6
         */
        if (!value.matches(BIN_FORMAT)) {
            throw new IllegalArgumentException("Invalid card BIN doesn't match format: " + BIN_FORMAT);
        }
    }
}
