package it.pagopa.ecommerce.commons.domain.v1;

import io.vavr.control.Either;
import it.pagopa.ecommerce.commons.documents.v1.*;
import it.pagopa.ecommerce.commons.domain.v1.pojos.*;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Optional;

/**
 * <p>
 * Transaction with a closure error.
 * </p>
 * <p>
 * Applicable events with resulting aggregates are: if transaction was
 * previously <strong>authorized</strong>:
 * </p>
 * <ul>
 * <li>{@link TransactionClosedEvent} --> {@link TransactionClosed}</li>
 * <li>{@link TransactionExpiredEvent} --> {@link TransactionExpired}</li>
 * <li>{@link TransactionRefundRequestedEvent} -->
 * {@link TransactionWithRefundRequested}</li>
 * <li>{@link TransactionClosureFailedEvent} -->
 * {@link TransactionUnauthorized}</li>
 * </ul>
 * <p>
 * if transaction was <strong> NOT authorized</strong>:
 * </p>
 * <ul>
 * <li>{@link TransactionClosedEvent} --> {@link TransactionUserCanceled}</li>
 * <li>{@link TransactionExpiredEvent} -->
 * {@link TransactionCancellationExpired}</li>
 * </ul>
 * <p>
 * Other events than the above ones will be discarded
 * </p>
 * <p>
 * Semantically this means that the transaction has recovered from the closure
 * error.
 * </p>
 *
 * @see Transaction
 * @see BaseTransactionWithClosureError
 */
@EqualsAndHashCode(callSuper = true)
@ToString
public final class TransactionWithClosureError extends BaseTransactionWithClosureError
        implements Transaction {

    /**
     * Primary constructor
     *
     * @param baseTransaction base transaction
     * @param event           closure error event
     */
    public TransactionWithClosureError(
            BaseTransactionWithPaymentToken baseTransaction,
            TransactionClosureErrorEvent event
    ) {
        super(baseTransaction, event);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Transaction applyEvent(Object event) {
        Optional<Either<BaseTransactionWithCancellationRequested, BaseTransactionWithCompletedAuthorization>> transactionAtPreviousState = transactionAtPreviousState();

        if (transactionAtPreviousState.isEmpty()) {
            return this;
        }

        Either<BaseTransactionWithCancellationRequested, BaseTransactionWithCompletedAuthorization> either = transactionAtPreviousState
                .get();

        return either.fold(
                // Left: BaseTransactionWithCancellationRequested
                trxWithCancellation -> {
                    if (event instanceof TransactionClosedEvent transactionClosedEvent) {
                        return new TransactionUserCanceled(trxWithCancellation, transactionClosedEvent);
                    }
                    if (event instanceof TransactionExpiredEvent transactionExpiredEvent) {
                        return new TransactionCancellationExpired(trxWithCancellation, transactionExpiredEvent);
                    }
                    return this;
                },

                // Right: BaseTransactionWithCompletedAuthorization
                trxWithAuthorizationCompleted -> {
                    boolean wasTransactionAuthorized = trxWithAuthorizationCompleted.wasTransactionAuthorized();

                    if (event instanceof TransactionClosedEvent transactionClosedEvent && wasTransactionAuthorized) {
                        return new TransactionClosed(trxWithAuthorizationCompleted, transactionClosedEvent);
                    }
                    if (event instanceof TransactionExpiredEvent transactionExpiredEvent) {
                        return new TransactionExpired(trxWithAuthorizationCompleted, transactionExpiredEvent);
                    }
                    if (event instanceof TransactionRefundRequestedEvent transactionRefundRequestedEvent) {
                        return new TransactionWithRefundRequested(
                                trxWithAuthorizationCompleted,
                                transactionRefundRequestedEvent
                        );
                    }
                    if (event instanceof TransactionClosureFailedEvent transactionClosureFailedEvent
                            && !wasTransactionAuthorized) {
                        return new TransactionUnauthorized(
                                trxWithAuthorizationCompleted,
                                transactionClosureFailedEvent
                        );
                    }
                    return this;
                }
        );
    }

    /**
     * Return an Either of the transaction at previous point
     *
     * @return an optional Either instance populated from the transaction value at
     *         previous step. If the transaction is not one of
     *         {@link BaseTransactionWithCancellationRequested} or
     *         {@link BaseTransactionWithRequestedAuthorization} then an empty
     *         Optional is returned
     */
    public Optional<Either<BaseTransactionWithCancellationRequested, BaseTransactionWithCompletedAuthorization>> transactionAtPreviousState() {
        Object prevState = this.getTransactionAtPreviousState();
        if (prevState instanceof BaseTransactionWithCancellationRequested baseTransactionWithCancellationRequested) {
            return Optional.of(Either.left(baseTransactionWithCancellationRequested));
        } else if (prevState instanceof BaseTransactionWithCompletedAuthorization baseTransactionWithCompletedAuthorization) {
            return Optional.of(Either.right(baseTransactionWithCompletedAuthorization));
        } else {
            return Optional.empty();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TransactionStatusDto getStatus() {
        return TransactionStatusDto.CLOSURE_ERROR;
    }
}
