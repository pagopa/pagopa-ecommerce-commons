package it.pagopa.ecommerce.commons.domain;

import it.pagopa.ecommerce.commons.annotations.ValueObject;

/**
 * <p>A payment token. This allows a client to execute a payment in the pagoPA platform.</p>
 * <p>Note that a single token is subject to expiry and may be used multiple times according to the Nodo API specifications.</p>
 * @param value raw value of the payment token
 */
@ValueObject
public record PaymentToken(String value) {}
