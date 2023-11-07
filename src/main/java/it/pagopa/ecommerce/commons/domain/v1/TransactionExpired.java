package it.pagopa.ecommerce.commons.domain.v1;

import it.pagopa.ecommerce.commons.documents.v1.TransactionExpiredEvent;
import it.pagopa.ecommerce.commons.documents.v1.TransactionRefundRequestedEvent;
import it.pagopa.ecommerce.commons.documents.v1.TransactionUserReceiptRequestedEvent;
import it.pagopa.ecommerce.commons.domain.v1.pojos.BaseTransactionClosed;
import it.pagopa.ecommerce.commons.domain.v1.pojos.BaseTransactionExpired;
import it.pagopa.ecommerce.commons.domain.v1.pojos.BaseTransactionWithRequestedAuthorization;
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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Transaction applyEvent(Object event) {
        return switch (event) {
            case TransactionRefundRequestedEvent e -> new TransactionWithRefundRequested(
                    this.getTransactionAtPreviousState(),
                    e
            );
            case TransactionUserReceiptRequestedEvent e -> {
                if (getTransactionAtPreviousState() instanceof BaseTransactionClosed baseTransactionClosed) {
                    yield new TransactionWithRequestedUserReceipt(baseTransactionClosed, e);
                } else {
                    yield this;
                }
            }
            default -> this;
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TransactionStatusDto getStatus() {
        return TransactionStatusDto.EXPIRED;
    }
}
