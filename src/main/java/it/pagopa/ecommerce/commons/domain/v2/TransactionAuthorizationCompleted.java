package it.pagopa.ecommerce.commons.domain.v2;

import it.pagopa.ecommerce.commons.documents.v2.*;
import it.pagopa.ecommerce.commons.domain.v2.pojos.BaseTransactionWithCompletedAuthorization;
import it.pagopa.ecommerce.commons.domain.v2.pojos.BaseTransactionWithRequestedAuthorization;
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
 * Applicable events with resulting aggregates are:
 * <ul>
 * <li>{@link TransactionClosedEvent} --> {@link TransactionClosed}</li>
 * <li>{@link TransactionClosureErrorEvent} -->
 * {@link TransactionWithClosureError}</li>
 * <li>{@link TransactionExpiredEvent} --> {@link TransactionExpired}</li>
 * <li>{@link TransactionClosureFailedEvent} -->
 * {@link TransactionUnauthorized}</li>
 * </ul>
 * Any other event than the above ones will be discarded.
 *
 * @see it.pagopa.ecommerce.commons.domain.v2.Transaction
 * @see BaseTransactionWithCompletedAuthorization
 */
@EqualsAndHashCode(callSuper = true)
@ToString
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public final class TransactionAuthorizationCompleted extends BaseTransactionWithCompletedAuthorization
        implements it.pagopa.ecommerce.commons.domain.v2.Transaction {

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
        if (event instanceof TransactionClosureRequestedEvent) {
            return new TransactionWithClosureRequested(this);
        } else if (event instanceof TransactionExpiredEvent transactionExpiredEvent) {
            return new TransactionExpired(this, transactionExpiredEvent);
        }
        return this;
    }

    @Override
    public TransactionStatusDto getStatus() {
        return TransactionStatusDto.AUTHORIZATION_COMPLETED;
    }
}
