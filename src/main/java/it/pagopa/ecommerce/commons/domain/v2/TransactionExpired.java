package it.pagopa.ecommerce.commons.domain.v2;

import it.pagopa.ecommerce.commons.documents.v2.TransactionClosureSyntheticEvent;
import it.pagopa.ecommerce.commons.documents.v2.TransactionExpiredEvent;
import it.pagopa.ecommerce.commons.documents.v2.TransactionRefundRequestedEvent;
import it.pagopa.ecommerce.commons.documents.v2.TransactionUserReceiptRequestedEvent;
import it.pagopa.ecommerce.commons.domain.v2.pojos.BaseTransactionClosed;
import it.pagopa.ecommerce.commons.domain.v2.pojos.BaseTransactionExpired;
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
         * in case of closure synthetic event the transaction will remain in EXPIRED
         * status. the transaction at previous state aggregate is recalculated applying
         * the closure synthetic event to effectively recalculate aggregate. This logic
         * reflects the fact that a transaction can recover from EXPIRED status with an
         * addUserReceipt request from Nodo and should handle closed transactions coming
         * from both close event and synthetic one but the closure event itself.
         */
        if (event instanceof TransactionClosureSyntheticEvent transactionClosureSyntheticEvent) {
            BaseTransactionWithRequestedAuthorization transactionAtPreviousState = this.getTransactionAtPreviousState();
            BaseTransactionWithRequestedAuthorization recalculatedBaseTransaction = (BaseTransactionWithRequestedAuthorization) ((Transaction) transactionAtPreviousState)
                    .applyEvent(transactionClosureSyntheticEvent);
            return new TransactionExpired(recalculatedBaseTransaction, this.event);
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
