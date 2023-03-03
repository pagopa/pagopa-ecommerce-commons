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
            BaseTransaction baseTransaction,
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
        return transactionAtPreviousState
                .map(either -> either.fold(
                        trxWithCancellation -> switch (event) {
                            case TransactionClosedEvent e -> new TransactionUserCanceled(trxWithCancellation, e);
                            case TransactionExpiredEvent e ->
                                    new TransactionCancellationExpired(trxWithCancellation, e);
                            default -> this;
                        },
                        trxWithAuthorizationCompleted -> switch (event) {
                            case TransactionClosedEvent e -> new TransactionClosed(trxWithAuthorizationCompleted, e);
                            case TransactionExpiredEvent e -> new TransactionExpired(trxWithAuthorizationCompleted, e);
                            case TransactionRefundRequestedEvent e ->
                                    new TransactionWithRefundRequested(trxWithAuthorizationCompleted, e);
                            case TransactionClosureFailedEvent e ->
                                    new TransactionUnauthorized(trxWithAuthorizationCompleted, e);
                            default -> this;
                        }
                ))
                .orElse(this);

    }

    /**
     * Return an Either of the transaction at previous point
     *
     * @return an optional Either instance populated from the transaction value at previous step.
     * If the transaction is not one of  {@link BaseTransactionWithCancellationRequested} or {@link BaseTransactionWithRequestedAuthorization} then an empty
     * Optional is returned
     */
    public Optional<Either<BaseTransactionWithCancellationRequested, BaseTransactionWithCompletedAuthorization>> transactionAtPreviousState() {
        return switch (this.getTransactionAtPreviousState()) {
            case BaseTransactionWithCancellationRequested trx -> Optional.of(Either.left(trx));
            case BaseTransactionWithCompletedAuthorization trx -> Optional.of(Either.right(trx));
            default -> Optional.empty();
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TransactionStatusDto getStatus() {
        return TransactionStatusDto.CLOSURE_ERROR;
    }
}
