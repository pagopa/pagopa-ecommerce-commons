package it.pagopa.ecommerce.commons.domain.v2;

import it.pagopa.ecommerce.commons.documents.v2.TransactionClosedEvent;
import it.pagopa.ecommerce.commons.documents.v2.TransactionClosureErrorEvent;
import it.pagopa.ecommerce.commons.documents.v2.TransactionExpiredEvent;
import it.pagopa.ecommerce.commons.documents.v2.TransactionUserCanceledEvent;
import it.pagopa.ecommerce.commons.domain.v2.pojos.BaseTransactionWithCancellationRequested;
import it.pagopa.ecommerce.commons.domain.v2.pojos.BaseTransactionWithPaymentToken;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * Transaction with cancellation requested by user. This is a transient state.
 * Applicable events with resulting aggregates are:
 * <ul>
 * <li>{@link TransactionClosedEvent} --> {@link TransactionUserCanceled}</li>
 * <li>{@link TransactionClosureErrorEvent} -->
 * {@link TransactionWithClosureError}</li>
 * <li>{@link TransactionExpiredEvent} -->
 * {@link TransactionCancellationExpired}</li>
 * </ul>
 * Any other event than the above ones will be discarded.
 *
 * @see Transaction
 * @see BaseTransactionWithCancellationRequested
 */
@EqualsAndHashCode(callSuper = true)
@ToString
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public final class TransactionWithCancellationRequested extends BaseTransactionWithCancellationRequested
        implements Transaction {

    TransactionUserCanceledEvent transactionUserCanceledEvent;

    /**
     * Primary constructor
     *
     * @param baseTransaction              the base transaction
     * @param transactionUserCanceledEvent the transaction expired event
     */
    public TransactionWithCancellationRequested(
            BaseTransactionWithPaymentToken baseTransaction,
            TransactionUserCanceledEvent transactionUserCanceledEvent
    ) {
        super(
                baseTransaction
        );
        this.transactionUserCanceledEvent = transactionUserCanceledEvent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TransactionStatusDto getStatus() {
        return TransactionStatusDto.CANCELLATION_REQUESTED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Transaction applyEvent(Object event) {
        if (event instanceof TransactionClosedEvent transactionClosedEvent) {
            return new TransactionUserCanceled(this, transactionClosedEvent);
        } else if (event instanceof TransactionClosureErrorEvent transactionClosureErrorEvent) {
            return new TransactionWithClosureError(this, transactionClosureErrorEvent);
        } else if (event instanceof TransactionExpiredEvent transactionExpiredEvent) {
            return new TransactionCancellationExpired(this, transactionExpiredEvent);
        }
        return this;
    }
}
