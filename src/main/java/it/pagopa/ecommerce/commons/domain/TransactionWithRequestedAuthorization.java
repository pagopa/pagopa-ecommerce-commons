package it.pagopa.ecommerce.commons.domain;

import it.pagopa.ecommerce.commons.documents.*;
import it.pagopa.ecommerce.commons.domain.pojos.BaseTransactionWithPaymentToken;
import it.pagopa.ecommerce.commons.domain.pojos.BaseTransactionWithRequestedAuthorization;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * Transaction with a requested authorization.
 * </p>
 * <p>
 * To this class you can apply an
 * {@link it.pagopa.ecommerce.commons.documents.TransactionAuthorizedEvent} to
 * get a {@link it.pagopa.ecommerce.commons.domain.TransactionAuthorized} or a
 * {@link it.pagopa.ecommerce.commons.documents.TransactionAuthorizationFailedEvent}
 * to get a
 * {@link it.pagopa.ecommerce.commons.domain.TransactionWithFailedAuthorization}
 * </p>
 *
 * @see Transaction
 * @see BaseTransactionWithRequestedAuthorization
 */
@EqualsAndHashCode(callSuper = true)
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

    /** {@inheritDoc} */
    @Override
    public Transaction applyEvent(Object event) {
        return switch (event) {
            case TransactionAuthorizedEvent authorizedEvent -> new TransactionAuthorized(this, authorizedEvent.getData());
            case TransactionAuthorizationFailedEvent authorizationFailedEvent -> new TransactionWithFailedAuthorization(this, authorizationFailedEvent);
            case TransactionExpiredEvent transactionExpiredEvent -> new TransactionExpired(this, transactionExpiredEvent);
            default -> this;
        };
    }

    /** {@inheritDoc} */
    @Override
    public TransactionStatusDto getStatus() {
        return TransactionStatusDto.AUTHORIZATION_REQUESTED;
    }
}
