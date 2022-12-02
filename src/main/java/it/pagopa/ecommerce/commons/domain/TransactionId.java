package it.pagopa.ecommerce.commons.domain;

import it.pagopa.ecommerce.commons.annotations.ValueObject;
import java.util.UUID;

/**
 * <p>
 * Value object holding a transaction id.
 * </p>
 *
 * @param value the transaction id
 */
@ValueObject
public record TransactionId(UUID value) {
}
