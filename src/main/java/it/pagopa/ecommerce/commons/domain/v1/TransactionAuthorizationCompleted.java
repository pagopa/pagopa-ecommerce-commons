package it.pagopa.ecommerce.commons.domain.v1;

import it.pagopa.ecommerce.commons.documents.v1.*;
import it.pagopa.ecommerce.commons.domain.v1.pojos.BaseTransactionWithCompletedAuthorization;
import it.pagopa.ecommerce.commons.domain.v1.pojos.BaseTransactionWithRequestedAuthorization;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * <p>
 * Transaction with a successful authorization.
 * </p>
 * <p>
 * To this class you can apply either a {@link TransactionClosedEvent} to get a
 * {@link TransactionClosed} or a {@link TransactionClosureErrorEvent} to get a
 * {@link TransactionWithClosureError}
 * </p>
 *
 * @see Transaction
 * @see BaseTransactionWithCompletedAuthorization
 */
@EqualsAndHashCode(callSuper = true)
@ToString
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public final class TransactionAuthorizationCompleted extends BaseTransactionWithCompletedAuthorization
        implements Transaction {

    TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent;

    /**
     * Primary constructor
     *
     * @param baseTransaction                        base transaction
     * @param transactionAuthorizationCompletedEvent the transaction authorization
     *                                               requested event
     */
    public TransactionAuthorizationCompleted(
            BaseTransactionWithRequestedAuthorization baseTransaction,
            TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent
    ) {
        super(baseTransaction, transactionAuthorizationCompletedEvent.getData());
        this.transactionAuthorizationCompletedEvent = transactionAuthorizationCompletedEvent;
    }

    @Override
    public Transaction applyEvent(Object event) {
        return switch (event) {
            case TransactionClosedEvent closureSentEvent -> new TransactionClosed(
                    this,
                    closureSentEvent);
            case TransactionClosureErrorEvent closureErrorEvent -> new TransactionWithClosureError(
                    this,
                    closureErrorEvent
            );
            case TransactionExpiredEvent transactionExpiredEvent ->
                    new TransactionExpired(this, transactionExpiredEvent);
            case TransactionClosureFailedEvent transactionClosureFailedEvent ->
                    new TransactionUnauthorized(this, transactionClosureFailedEvent);
            default -> this;
        };
    }

    @Override
    public TransactionStatusDto getStatus() {
        return TransactionStatusDto.AUTHORIZATION_COMPLETED;
    }
}
