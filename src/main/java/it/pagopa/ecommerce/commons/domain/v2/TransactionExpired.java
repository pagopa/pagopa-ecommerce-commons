package it.pagopa.ecommerce.commons.domain.v2;

import it.pagopa.ecommerce.commons.documents.v2.TransactionClosureSyntheticEvent;
import it.pagopa.ecommerce.commons.documents.v2.TransactionExpiredEvent;
import it.pagopa.ecommerce.commons.documents.v2.TransactionRefundRequestedEvent;
import it.pagopa.ecommerce.commons.documents.v2.TransactionUserReceiptRequestedEvent;
import it.pagopa.ecommerce.commons.domain.v2.pojos.BaseTransactionClosed;
import it.pagopa.ecommerce.commons.domain.v2.pojos.BaseTransactionExpired;
import it.pagopa.ecommerce.commons.domain.v2.pojos.BaseTransactionWithClosureRequested;
import it.pagopa.ecommerce.commons.domain.v2.pojos.BaseTransactionWithRequestedAuthorization;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * <p>
 * Expired transaction.
 * </p>
 * Applicable events with resulting aggregates are:
 * <ul>
 * <li>{@link TransactionRefundRequestedEvent} -->
 * {@link TransactionWithRefundRequested}</li>
 * <li>{@link TransactionClosureSyntheticEvent} -->
 * {@link TransactionClosed}</li>
 * <li>{@link TransactionUserReceiptRequestedEvent} -->
 * {@link TransactionWithRequestedUserReceipt}</li>
 * </ul>
 * Any other event than the above ones will be discarded.
 *
 * @see Transaction
 * @see BaseTransactionExpired
 */
@EqualsAndHashCode(callSuper = true)
@ToString
public final class TransactionExpired extends BaseTransactionExpired implements Transaction {
    /**
     * The transaction expired event instance, used to perform re-aggregation
     */
    private final TransactionExpiredEvent event;

    /**
     * Primary constructor
     *
     * @param baseTransaction base transaction
     * @param event           expiration event
     */
    public TransactionExpired(
            BaseTransactionWithRequestedAuthorization baseTransaction,
            TransactionExpiredEvent event
    ) {
        super(baseTransaction, event.getData());
        this.event = event;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Transaction applyEvent(Object event) {
        if (event instanceof TransactionRefundRequestedEvent transactionRefundRequestedEvent) {
            return new TransactionWithRefundRequested(
                    this.getTransactionAtPreviousState(),
                    transactionRefundRequestedEvent
            );
        }
        /*
         * transaction with closure error, in case of expiration create two distinct
         * aggregates: - TransactionExpired -> if closure error comes from the state
         * machine flow where an authorization was requested -
         * TransactionCancellationExpired -> if closure error comes from the state
         * machine flow where the user cancel the transaction (no authorization was
         * requested).
         *
         * In the first case the TransactionExpired aggregate
         * "transactionAtPreviousStep" is valued with the
         * BaseTransactionWithClosureRequested one so the below check cover both
         * CLOSURE_REQUESTED and CLOSURE_ERROR transaction statuses
         */
        if (event instanceof TransactionClosureSyntheticEvent transactionClosureSyntheticEvent
                && getTransactionAtPreviousState()instanceof BaseTransactionWithClosureRequested txWithClosureRequested
                && txWithClosureRequested.wasTransactionAuthorized()) {
            return new TransactionClosed(txWithClosureRequested, transactionClosureSyntheticEvent);
        }

        if (event instanceof TransactionUserReceiptRequestedEvent transactionUserReceiptRequestedEvent &&
                getTransactionAtPreviousState()instanceof BaseTransactionClosed baseTransactionClosed) {
            return new TransactionWithRequestedUserReceipt(baseTransactionClosed, transactionUserReceiptRequestedEvent);
        }

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TransactionStatusDto getStatus() {
        return TransactionStatusDto.EXPIRED;
    }
}
