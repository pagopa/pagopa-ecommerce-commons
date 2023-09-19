package it.pagopa.ecommerce.commons.domain;

import it.pagopa.ecommerce.commons.annotations.ValueObject;

/**
 * <p>
 * Value object for a transaction amount. The amount is specified in euro cents.
 * </p>
 *
 * @param value amount in euro cents
 */
@ValueObject
public record TransactionAmount(int value) {
}
