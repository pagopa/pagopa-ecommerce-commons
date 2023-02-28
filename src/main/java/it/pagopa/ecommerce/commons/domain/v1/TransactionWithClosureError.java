package it.pagopa.ecommerce.commons.domain.v1;

import it.pagopa.ecommerce.commons.documents.v1.TransactionClosedEvent;
import it.pagopa.ecommerce.commons.documents.v1.TransactionClosureErrorEvent;
import it.pagopa.ecommerce.commons.documents.v1.TransactionExpiredEvent;
import it.pagopa.ecommerce.commons.documents.v1.TransactionRefundedEvent;
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
 * To this class you can apply a {@link TransactionClosedEvent} to get a
 * {@link TransactionClosedWithCompletedAuthorization} or
 * {@link TransactionClosedWithoutCompletedAuthorization} based on the fact that
 * the transaction was previously authorized. Semantically this means that the
 * transaction has recovered from the closure error.
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
        return switch (event) {
            case TransactionClosedEvent closureSentEvent -> {
                if (this.getTransactionAtPreviousState() instanceof BaseTransactionWithCompletedAuthorization baseTransactionWithCompletedAuthorization) {
                    yield new TransactionClosedWithCompletedAuthorization(
                            baseTransactionWithCompletedAuthorization,
                            closureSentEvent);
                } else {
                    yield new TransactionClosedWithoutCompletedAuthorization(
                            this,
                            closureSentEvent);
                }
            }
            case TransactionExpiredEvent transactionExpiredEvent ->
                    new TransactionExpired(this, transactionExpiredEvent);
            case TransactionRefundedEvent transactionRefundedEvent ->
                    new TransactionRefunded(this, transactionRefundedEvent);
            default -> this;
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
