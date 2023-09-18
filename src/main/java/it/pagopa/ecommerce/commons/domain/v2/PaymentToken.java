package it.pagopa.ecommerce.commons.domain.v2;

import it.pagopa.ecommerce.commons.annotations.ValueObject;

/**
 * A payment token. This allows a client to execute a payment in the pagoPA
 * platform.
 *
 * <p>
 * Note that a single token is subject to expiry and may be used multiple times
 * according to the Nodo API specifications.
 *
 * @param value raw value of the payment token
 */
@ValueObject
public record PaymentToken(String value) {
}
