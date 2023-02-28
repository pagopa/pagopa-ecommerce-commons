package it.pagopa.ecommerce.commons.domain.v1;

import it.pagopa.ecommerce.commons.documents.v1.TransactionExpiredEvent;
import it.pagopa.ecommerce.commons.documents.v1.TransactionRefundErrorEvent;
import it.pagopa.ecommerce.commons.documents.v1.TransactionRefundRequestedEvent;
import it.pagopa.ecommerce.commons.domain.v1.pojos.BaseTransaction;
import it.pagopa.ecommerce.commons.domain.v1.pojos.BaseTransactionClosureWithoutAuthorization;
import it.pagopa.ecommerce.commons.domain.v1.pojos.BaseTransactionExpired;
import it.pagopa.ecommerce.commons.domain.v1.pojos.BaseTransactionWithCompletedAuthorization;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * <p>
 * Expired transaction.
 * </p>
 *
 * @see Transaction
 * @see BaseTransactionClosureWithoutAuthorization
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
        //transition from expiry to refund requested is allowed only if the transaction was previously authorized
        if (this.getTransactionAtPreviousState() instanceof BaseTransactionWithCompletedAuthorization baseTransactionWithCompletedAuthorization) {
            return switch (event) {
                case TransactionRefundErrorEvent e ->
                        new TransactionWithRefundError(baseTransactionWithCompletedAuthorization, e);
                case TransactionRefundRequestedEvent e ->
                        new TransactionWithRefundRequested(baseTransactionWithCompletedAuthorization, e);
                default -> this;
            };
        } else {
            return this;
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TransactionStatusDto getStatus() {
        return TransactionStatusDto.EXPIRED;
    }
}
