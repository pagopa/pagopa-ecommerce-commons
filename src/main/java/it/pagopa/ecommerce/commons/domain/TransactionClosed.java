package it.pagopa.ecommerce.commons.domain;

import it.pagopa.ecommerce.commons.documents.TransactionClosureSendData;
import it.pagopa.ecommerce.commons.documents.TransactionExpiredEvent;
import it.pagopa.ecommerce.commons.documents.TransactionUserReceiptAddedEvent;
import it.pagopa.ecommerce.commons.domain.pojos.BaseTransactionClosed;
import it.pagopa.ecommerce.commons.domain.pojos.BaseTransactionWithCompletedAuthorization;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * Closed transaction.
 * </p>
 * <p>
 * To this class you can apply a
 * {@link it.pagopa.ecommerce.commons.documents.TransactionUserReceiptAddedEvent}
 * to get a
 * {@link it.pagopa.ecommerce.commons.domain.TransactionWithUserReceipt}.
 * </p>
 *
 * @see Transaction
 * @see BaseTransactionWithCompletedAuthorization
 */
@EqualsAndHashCode(callSuper = true)
public final class TransactionClosed extends BaseTransactionClosed implements Transaction {

    /**
     * Primary constructor
     *
     * @param baseTransaction      base transaction
     * @param closureSentEventData data related to closure sending event
     */
    public TransactionClosed(
            BaseTransactionWithCompletedAuthorization baseTransaction,
            TransactionClosureSendData closureSentEventData
    ) {
        super(baseTransaction, closureSentEventData);
    }

    /** {@inheritDoc} */
    @Override
    public Transaction applyEvent(Object event) {
        return switch (event) {
            case TransactionUserReceiptAddedEvent e -> new TransactionWithUserReceipt(this, e.getData());
            case TransactionExpiredEvent transactionExpiredEvent -> new TransactionExpired(this, transactionExpiredEvent);
            default -> this;
        };
    }

    /** {@inheritDoc} */
    @Override
    public TransactionStatusDto getStatus() {
        return this.getTransactionClosureSendData().getNewTransactionStatus();
    }
}
