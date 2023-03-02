package it.pagopa.ecommerce.commons.domain.v1;

import it.pagopa.ecommerce.commons.documents.v1.*;
import it.pagopa.ecommerce.commons.domain.v1.pojos.BaseTransaction;
import it.pagopa.ecommerce.commons.domain.v1.pojos.BaseTransactionWithCancellationRequested;
import it.pagopa.ecommerce.commons.domain.v1.pojos.BaseTransactionWithClosureError;
import it.pagopa.ecommerce.commons.domain.v1.pojos.BaseTransactionWithCompletedAuthorization;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.EqualsAndHashCode;
import lombok.ToString;

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
 * <li>{@link TransactionExpiredEvent} --> {@link TransactionExpired}</li>
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

        if (wasTransactionAuthorized()) {
            BaseTransactionWithCompletedAuthorization baseTransaction = (BaseTransactionWithCompletedAuthorization) this.getTransactionAtPreviousState();
            return switch (event) {
                case TransactionClosedEvent e -> new TransactionClosed(baseTransaction, e);
                case TransactionExpiredEvent e -> new TransactionExpired(baseTransaction, e);
                case TransactionRefundRequestedEvent e -> new TransactionWithRefundRequested(baseTransaction, e);
                case TransactionClosureFailedEvent e -> new TransactionUnauthorized(baseTransaction, e);
                default -> this;
            };
        }
        if (wasCancelledByUser()) {
            BaseTransactionWithCancellationRequested baseTransaction = (BaseTransactionWithCancellationRequested) this.getTransactionAtPreviousState();
            return switch (event) {
                case TransactionClosedEvent e -> new TransactionUserCanceled(baseTransaction, e);
                case TransactionExpiredEvent e -> new TransactionCancellationExpired(baseTransaction, e);
                default -> this;
            };
        }
        return this;

    }

    /**
     * Checks if the transaction was previously authorized by checking the type of
     * the transaction at previous state
     *
     * @return true if the transaction was previously authorized, false otherwise
     */
    private boolean wasTransactionAuthorized() {
        return this.getTransactionAtPreviousState() instanceof BaseTransactionWithCompletedAuthorization;
    }

    /**
     * Checks if the transaction was cancelled by the user by checking the type of
     * the transaction at previous state
     *
     * @return true if the transaction was cancelled by the user, false otherwise
     */
    private boolean wasCancelledByUser() {
        return this.getTransactionAtPreviousState() instanceof BaseTransactionWithCancellationRequested;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TransactionStatusDto getStatus() {
        return TransactionStatusDto.CLOSURE_ERROR;
    }
}
