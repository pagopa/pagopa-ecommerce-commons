package it.pagopa.ecommerce.commons.domain;

import it.pagopa.ecommerce.commons.documents.TransactionClosureSendData;
import it.pagopa.ecommerce.commons.domain.pojos.BaseTransactionAuthorized;
import it.pagopa.ecommerce.commons.domain.pojos.BaseTransactionClosed;
import it.pagopa.ecommerce.commons.domain.pojos.BaseTransactionWithCompletedAuthorization;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * Closed transaction.
 * </p>
 * <p>
 * Given that this is a terminal state for a transaction, there are no events
 * that you can meaningfully apply to it. Any event application is thus ignored.
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
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public TransactionStatusDto getStatus() {
        return this.getTransactionClosureSendData().getNewTransactionStatus();
    }
}
