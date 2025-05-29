package it.pagopa.ecommerce.commons.domain.v2;

import it.pagopa.ecommerce.commons.documents.v2.TransactionClosedEvent;
import it.pagopa.ecommerce.commons.documents.v2.TransactionExpiredEvent;
import it.pagopa.ecommerce.commons.documents.v2.TransactionRefundRequestedEvent;
import it.pagopa.ecommerce.commons.documents.v2.TransactionUserReceiptRequestedEvent;
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
     * Primary constructor
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
     * {@inheritDoc}
     */
    @Override
    public Transaction applyEvent(Object event) {
        if (event instanceof TransactionExpiredEvent e) {
            return new TransactionExpired(this, e);
        } else if (event instanceof TransactionRefundRequestedEvent e) {
            return new TransactionWithRefundRequested(this, e);
        } else if (event instanceof TransactionUserReceiptRequestedEvent e) {
            return new TransactionWithRequestedUserReceipt(this, e);
        } else {
            return this;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TransactionStatusDto getStatus() {
        return TransactionStatusDto.CLOSED;
    }
}
