package it.pagopa.ecommerce.commons.domain;

import it.pagopa.ecommerce.commons.annotations.ValueObject;

/**
 * <p>
 * Value object for a payment context code. .
 * </p>
 *
 * @param value the payment context code string
 */
@ValueObject
public record PaymentContextCode(String value) {
}
