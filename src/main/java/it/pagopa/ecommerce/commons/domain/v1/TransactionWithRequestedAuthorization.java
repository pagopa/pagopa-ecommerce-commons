package it.pagopa.ecommerce.commons.domain.v1;

import it.pagopa.ecommerce.commons.documents.v1.TransactionAuthorizationCompletedEvent;
import it.pagopa.ecommerce.commons.documents.v1.TransactionAuthorizationRequestData;
import it.pagopa.ecommerce.commons.documents.v1.TransactionExpiredEvent;
import it.pagopa.ecommerce.commons.domain.v1.pojos.BaseTransactionWithPaymentToken;
import it.pagopa.ecommerce.commons.domain.v1.pojos.BaseTransactionWithRequestedAuthorization;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * <p>
 * Transaction with a requested authorization.
 * </p>
 * Applicable events with resulting aggregates are:
 * <ul>
 * <li>{@link TransactionAuthorizationCompletedEvent} -->
 * {@link TransactionAuthorizationCompleted}</li>
 * <li>{@link TransactionExpiredEvent} --> {@link TransactionExpired}</li>
 * </ul>
 * Other events than the above ones will be discarded
 *
 * @see Transaction
 * @see BaseTransactionWithRequestedAuthorization
 */
@EqualsAndHashCode(callSuper = true)
@ToString
public final class TransactionWithRequestedAuthorization extends BaseTransactionWithRequestedAuthorization
        implements
        Transaction {

    /**
     * Primary constructor
     *
     * @param transaction              base transaction
     * @param authorizationRequestData data related to authorization request
     */
    public TransactionWithRequestedAuthorization(
            BaseTransactionWithPaymentToken transaction,
            TransactionAuthorizationRequestData authorizationRequestData
    ) {
        super(transaction, authorizationRequestData);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Transaction applyEvent(Object event) {
        if (event instanceof TransactionAuthorizationCompletedEvent) {
            return new TransactionAuthorizationCompleted(this, (TransactionAuthorizationCompletedEvent) event);
        } else if (event instanceof TransactionExpiredEvent) {
            return new TransactionExpired(this, (TransactionExpiredEvent) event);
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TransactionStatusDto getStatus() {
        return TransactionStatusDto.AUTHORIZATION_REQUESTED;
    }
}
