package it.pagopa.ecommerce.commons.domain;

import it.pagopa.ecommerce.commons.documents.TransactionAuthorizationCompletedEvent;
import it.pagopa.ecommerce.commons.documents.TransactionAuthorizationRequestData;
import it.pagopa.ecommerce.commons.documents.TransactionExpiredEvent;
import it.pagopa.ecommerce.commons.domain.pojos.BaseTransactionWithPaymentToken;
import it.pagopa.ecommerce.commons.domain.pojos.BaseTransactionWithRequestedAuthorization;
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
 * {@link it.pagopa.ecommerce.commons.documents.TransactionExpiredEvent} to get
 * a {@link it.pagopa.ecommerce.commons.domain.TransactionExpired}
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
