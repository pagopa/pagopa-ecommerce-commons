package it.pagopa.ecommerce.commons.domain.v2;

import io.vavr.control.Either;
import it.pagopa.ecommerce.commons.documents.v2.*;
import it.pagopa.ecommerce.commons.domain.v2.pojos.*;
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
 * @see it.pagopa.ecommerce.commons.domain.v2.Transaction
 * @see BaseTransactionWithClosureError
 */
@EqualsAndHashCode(callSuper = true)
@ToString
public final class TransactionWithClosureError extends BaseTransactionWithClosureError
        implements it.pagopa.ecommerce.commons.domain.v2.Transaction {

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
        Optional<Either<BaseTransactionWithCancellationRequested, BaseTransactionWithClosureRequested>> transactionAtPreviousState = transactionAtPreviousState();

        if (transactionAtPreviousState.isEmpty()) {
            return this;
        }

        Either<BaseTransactionWithCancellationRequested, BaseTransactionWithClosureRequested> either = transactionAtPreviousState
                .get();

        if (either.isLeft()) {
            BaseTransactionWithCancellationRequested trxWithCancellation = either.getLeft();

            if (event instanceof TransactionClosedEvent transactionClosedEvent) {
                return new TransactionUserCanceled(trxWithCancellation, transactionClosedEvent);
            } else if (event instanceof TransactionExpiredEvent transactionExpiredEvent) {
                return new TransactionCancellationExpired(trxWithCancellation, transactionExpiredEvent);
            }
        } else {
            BaseTransactionWithClosureRequested trxWithClosureRequested = either.get();
            boolean wasTransactionAuthorized = trxWithClosureRequested.wasTransactionAuthorized();

            if (event instanceof TransactionClosedEvent transactionClosedEvent && wasTransactionAuthorized) {
                return new TransactionClosed(trxWithClosureRequested, transactionClosedEvent);
            } else if (event instanceof TransactionExpiredEvent transactionExpiredEvent) {
                return new TransactionExpired(trxWithClosureRequested, transactionExpiredEvent);
            } else if (event instanceof TransactionRefundRequestedEvent transactionRefundRequestedEvent) {
                return new TransactionWithRefundRequested(
                        trxWithClosureRequested,
                        transactionRefundRequestedEvent
                );
            } else if (event instanceof TransactionClosureFailedEvent transactionClosureFailedEvent
                    && !wasTransactionAuthorized) {
                return new TransactionUnauthorized(
                        trxWithClosureRequested,
                        transactionClosureFailedEvent
                );
            }
        }

        return this;
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
    public Optional<Either<BaseTransactionWithCancellationRequested, BaseTransactionWithClosureRequested>> transactionAtPreviousState() {
        Object prev = this.getTransactionAtPreviousState();
        if (prev instanceof BaseTransactionWithCancellationRequested) {
            return Optional.of(Either.left((BaseTransactionWithCancellationRequested) prev));
        } else if (prev instanceof BaseTransactionWithClosureRequested) {
            return Optional.of(Either.right((BaseTransactionWithClosureRequested) prev));
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
