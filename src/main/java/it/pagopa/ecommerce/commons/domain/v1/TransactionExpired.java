package it.pagopa.ecommerce.commons.domain.v1;

import it.pagopa.ecommerce.commons.documents.v1.TransactionExpiredEvent;
import it.pagopa.ecommerce.commons.documents.v1.TransactionRefundErrorEvent;
import it.pagopa.ecommerce.commons.documents.v1.TransactionRefundRequestedEvent;
import it.pagopa.ecommerce.commons.domain.v1.pojos.BaseTransaction;
import it.pagopa.ecommerce.commons.domain.v1.pojos.BaseTransactionExpired;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * <p>
 * Expired transaction.
 * </p>
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
            BaseTransaction baseTransaction,
            TransactionExpiredEvent event
    ) {
        super(baseTransaction, event.getData());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Transaction applyEvent(Object event) {
        /*
         * All statuses from which a transaction will come from have the authorization
         * been requested. The only status that has an exception from this is an expiration on ACTIVATED
         * status that will make the transaction goes to the EXPIRED_NOT_AUTHORIZED final state.
         * So is safe to assume that this applyEvent method will only be called for a transaction
         * for which an authorization request have been performed.
         * Given that no need to check if the authorization was previously authorized here is needed.
         */
        return switch (event) {
            case TransactionRefundErrorEvent e ->
                    new TransactionWithRefundError(this.getTransactionAtPreviousState(), e);
            case TransactionRefundRequestedEvent e ->
                    new TransactionWithRefundRequested(this.getTransactionAtPreviousState(), e);
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
