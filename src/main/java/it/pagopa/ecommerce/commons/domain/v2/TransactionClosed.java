package it.pagopa.ecommerce.commons.domain.v2;

import it.pagopa.ecommerce.commons.documents.v2.*;
import it.pagopa.ecommerce.commons.domain.v2.pojos.BaseTransactionClosed;
import it.pagopa.ecommerce.commons.domain.v2.pojos.BaseTransactionWithClosureRequested;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * <p>
 * Closed transaction.
 * </p>
 * Applicable events with resulting aggregates are:
 * <ul>
 * <li>{@link TransactionExpiredEvent} --> {@link TransactionExpired}</li>
 * <li>{@link TransactionRefundRequestedEvent} -->
 * {@link TransactionWithRefundRequested}</li>
 * <li>{@link TransactionUserReceiptRequestedEvent} -->
 * {@link TransactionWithRequestedUserReceipt}</li>
 * </ul>
 * Any other event than the above ones will be discarded.
 *
 * @see Transaction
 * @see BaseTransactionClosed
 */
@EqualsAndHashCode(callSuper = true)
@ToString
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public final class TransactionClosed extends BaseTransactionClosed
        implements Transaction {

    /**
     * Constructor that take in input a TransactionClosedEvent
     *
     * @param baseTransaction        base transaction
     * @param transactionClosedEvent the transaction closed event
     */
    public TransactionClosed(
            BaseTransactionWithClosureRequested baseTransaction,
            TransactionClosedEvent transactionClosedEvent
    ) {
        super(baseTransaction, transactionClosedEvent.getData());
    }

    /**
     * Constructor that take in input a transactionClosureSyntheticEvent
     *
     * @param baseTransaction                  base transaction
     * @param transactionClosureSyntheticEvent the transaction closure synthetic
     *                                         event
     */
    public TransactionClosed(
            BaseTransactionWithClosureRequested baseTransaction,
            TransactionClosureSyntheticEvent transactionClosureSyntheticEvent
    ) {
        super(baseTransaction, transactionClosureSyntheticEvent.getData());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Transaction applyEvent(Object event) {
        if (event instanceof TransactionExpiredEvent transactionExpiredEvent) {
            return new TransactionExpired(this, transactionExpiredEvent);
        } else if (event instanceof TransactionRefundRequestedEvent transactionRefundRequestedEvent) {
            return new TransactionWithRefundRequested(this, transactionRefundRequestedEvent);
        } else if (event instanceof TransactionUserReceiptRequestedEvent transactionUserReceiptRequestedEvent) {
            return new TransactionWithRequestedUserReceipt(this, transactionUserReceiptRequestedEvent);
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TransactionStatusDto getStatus() {
        return TransactionStatusDto.CLOSED;
    }
}
