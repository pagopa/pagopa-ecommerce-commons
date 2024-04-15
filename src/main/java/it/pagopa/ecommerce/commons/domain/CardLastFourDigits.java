package it.pagopa.ecommerce.commons.domain;

import java.util.Objects;

/**
 * Domain class representing the last four digits of a credit/debit card
 *
 * @param value the card's last four digits
 */
public record CardLastFourDigits(String value) {
    /**
     * Card last four digits format used to check for valid values
     */
    public static final String LAST_FOUR_DIGITS_FORMAT = "\\d{4}";

    /**
     * Primary constructor
     *
     * @param value the card's last four digits
     */
    public CardLastFourDigits {
        Objects.requireNonNull(value);

        if (!value.matches(LAST_FOUR_DIGITS_FORMAT)) {
            throw new IllegalArgumentException(
                    "Invalid card last four digits don't match format " + LAST_FOUR_DIGITS_FORMAT
            );
        }
    }
}
