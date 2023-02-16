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
 * <p>
 * To this class you can apply an {@link TransactionAuthorizationCompletedEvent}
 * to get a {@link TransactionAuthorizationCompleted} or a
 * {@link TransactionExpiredEvent} to get a {@link TransactionExpired}
 * </p>
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
        return switch (event) {
            case TransactionAuthorizationCompletedEvent authorizedEvent ->
                    new TransactionAuthorizationCompleted(this, authorizedEvent);
            case TransactionExpiredEvent transactionExpiredEvent ->
                    new TransactionExpired(this, transactionExpiredEvent);
            default -> this;
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TransactionStatusDto getStatus() {
        return TransactionStatusDto.AUTHORIZATION_REQUESTED;
    }
}
