package it.pagopa.ecommerce.commons.domain;

import it.pagopa.ecommerce.commons.documents.*;
import it.pagopa.ecommerce.commons.domain.pojos.BaseTransactionAuthorized;
import it.pagopa.ecommerce.commons.domain.pojos.BaseTransactionWithRequestedAuthorization;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * Transaction with a successful authorization.
 * </p>
 * <p>
 * To this class you can apply either a {@link TransactionClosedEvent} to get a
 * {@link it.pagopa.ecommerce.commons.domain.TransactionClosed} or a
 * {@link it.pagopa.ecommerce.commons.documents.TransactionClosureErrorEvent} to
 * get a {@link it.pagopa.ecommerce.commons.domain.TransactionWithClosureError}
 * </p>
 *
 * @see Transaction
 * @see BaseTransactionAuthorized
 */
@EqualsAndHashCode(callSuper = true)
public final class TransactionAuthorizationCompleted extends BaseTransactionAuthorized implements Transaction {
    /**
     * Primary constructor
     *
     * @param baseTransaction           base transaction
     * @param transactionAuthorizedData transaction authorization data
     */
    public TransactionAuthorizationCompleted(
            BaseTransactionWithRequestedAuthorization baseTransaction,
            TransactionAuthorizedData transactionAuthorizedData
    ) {
        super(baseTransaction, transactionAuthorizedData);
    }

    @Override
    public Transaction applyEvent(Object event) {
        return switch (event) {
            case TransactionClosedEvent closureSentEvent -> new TransactionClosed(
                    this
            );
            case TransactionClosureErrorEvent closureErrorEvent -> new TransactionWithClosureError(
                    this,
                    closureErrorEvent
            );
            case TransactionExpiredEvent transactionExpiredEvent ->
                    new TransactionExpired(this, transactionExpiredEvent);
            case TransactionClosureFailedEvent transactionClosureFailedEvent -> new TransactionUnauthorized(this);
            default -> this;
        };
    }

    @Override
    public TransactionStatusDto getStatus() {
        return TransactionStatusDto.AUTHORIZATION_COMPLETED;
    }
}
