package it.pagopa.ecommerce.commons.domain;

import it.pagopa.ecommerce.commons.annotations.ValueObject;

/**
 * <p>
 *     Value object holding a transaction description.
 * </p>
 * @param value the transaction description
 */
@ValueObject
public record TransactionDescription(String value) {
}
