package it.pagopa.ecommerce.commons.domain.v1;

import it.pagopa.ecommerce.commons.documents.v1.*;
import it.pagopa.ecommerce.commons.domain.v1.pojos.BaseTransaction;
import it.pagopa.ecommerce.commons.domain.v1.pojos.BaseTransactionClosureWithoutAuthorization;
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
 * {@link TransactionClosedEvent} -> {@link TransactionClosed}
 * {@link TransactionExpiredEvent} -> {@link TransactionExpired}
 * {@link TransactionRefundRequestedEvent} ->
 * {@link TransactionWithRefundRequested} {@link TransactionClosureFailedEvent}
 * -> {@link TransactionUnauthorized}
 * <p>
 * if transaction was <strong> NOT authorized</strong>:
 * </p>
 * {@link TransactionClosedEvent} -> {@link TransactionUserCanceled}
 * {@link TransactionExpiredEvent} -> {@link TransactionExpired}
 * <p>
 * Other events than the above ones will be discarded
 * </p>
 * <p>
 * Semantically this means that the transaction has recovered from the closure
 * error.
 * </p>
 *
 * @see Transaction
 * @see BaseTransactionClosureWithoutAuthorization
 */
@EqualsAndHashCode(callSuper = true)
@ToString
public final class TransactionWithClosureError extends BaseTransactionClosureWithoutAuthorization
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
        } else {
            BaseTransaction baseTransaction = this.getTransactionAtPreviousState();
            return switch (event) {
                case TransactionClosedEvent e -> new TransactionUserCanceled(baseTransaction, e);
                case TransactionExpiredEvent e -> new TransactionExpired(baseTransaction, e);
                default -> this;
            };
        }

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
     * {@inheritDoc}
     */
    @Override
    public TransactionStatusDto getStatus() {
        return TransactionStatusDto.CLOSURE_ERROR;
    }
}
