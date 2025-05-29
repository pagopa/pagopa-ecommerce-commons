package it.pagopa.ecommerce.commons.domain.v2;

import it.pagopa.ecommerce.commons.documents.v2.*;
import it.pagopa.ecommerce.commons.domain.v2.pojos.BaseTransactionClosed;
import it.pagopa.ecommerce.commons.domain.v2.pojos.BaseTransactionWithClosureRequested;
import it.pagopa.ecommerce.commons.domain.v2.pojos.BaseTransactionWithCompletedAuthorization;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * <p>
 * Closure requested transaction.
 * </p>
 * Applicable events with resulting aggregates are:
 * <ul>
 * <li>{@link TransactionExpiredEvent} -->
 * <li>{@link TransactionClosureFailedEvent} -->
 * {@link TransactionClosureErrorEvent}</li>
 * <li>{@link TransactionClosedEvent} -->
 * </ul>
 * Any other event than the above ones will be discarded.
 *
 * @see it.pagopa.ecommerce.commons.domain.v2.Transaction
 * @see BaseTransactionClosed
 */
@EqualsAndHashCode(callSuper = true)
@ToString
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public final class TransactionWithClosureRequested extends BaseTransactionWithClosureRequested implements Transaction {

    /**
     * Primary constructor
     *
     * @param baseTransaction base transaction
     */
    public TransactionWithClosureRequested(
            BaseTransactionWithCompletedAuthorization baseTransaction
    ) {
        super(baseTransaction);
    }

    @Override
    public Transaction applyEvent(Object event) {
        boolean wasTransactionAuthorized = wasTransactionAuthorized();

        if (event instanceof TransactionClosedEvent transactionClosedEvent && wasTransactionAuthorized) {
            return new TransactionClosed(this, transactionClosedEvent);
        }

        if (event instanceof TransactionClosureErrorEvent transactionClosureErrorEvent) {
            return new TransactionWithClosureError(this, transactionClosureErrorEvent);
        }

        if (event instanceof TransactionClosureFailedEvent transactionClosureFailedEvent && !wasTransactionAuthorized) {
            return new TransactionUnauthorized(this, transactionClosureFailedEvent);
        }

        if (event instanceof TransactionExpiredEvent transactionExpiredEvent) {
            return new TransactionExpired(this, transactionExpiredEvent);
        }

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TransactionStatusDto getStatus() {
        return TransactionStatusDto.CLOSURE_REQUESTED;
    }
}
