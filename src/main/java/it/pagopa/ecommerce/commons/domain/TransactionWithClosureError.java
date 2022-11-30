package it.pagopa.ecommerce.commons.domain;

import it.pagopa.ecommerce.commons.domain.pojos.BaseTransactionWithClosureError;
import it.pagopa.ecommerce.commons.domain.pojos.BaseTransactionWithCompletedAuthorization;
import it.pagopa.ecommerce.commons.generated.events.v1.TransactionClosureErrorEvent;
import it.pagopa.ecommerce.commons.generated.events.v1.TransactionClosureSentEvent;
import it.pagopa.generated.transactions.server.model.TransactionStatusDto;
import lombok.EqualsAndHashCode;

/**
 * <p>
 *     Transaction with a closure error.
 * </p>
 * <p>
 *     To this class you can apply a {@link TransactionClosureSentEvent} to get a {@link TransactionClosed}.
 *     Semantically this means that the transaction has recovered from the closure error.
 * </p>
 *
 * @see Transaction
 * @see BaseTransactionWithClosureError
 */
@EqualsAndHashCode(callSuper = true)
public final class TransactionWithClosureError extends BaseTransactionWithClosureError implements Transaction {

    /**
     * Primary constructor
     *
     * @param baseTransaction base transaction
     * @param event closure error event
     */
    public TransactionWithClosureError(
            BaseTransactionWithCompletedAuthorization baseTransaction,
            TransactionClosureErrorEvent event
    ) {
        super(baseTransaction, event);
    }

    @Override
    public Transaction applyEvent(Object event) {
        if (event instanceof TransactionClosureSentEvent closureSentEvent) {
            return new TransactionClosed(
                    this.withStatus(TransactionStatusDto.fromValue(closureSentEvent.getData().getNewTransactionStatus().value())),
                    closureSentEvent.getData()
            );
        } else {
            return this;
        }
    }

    /**
     * Change the transaction status
     * @param status new status
     * @return a new transaction with the same data except for the status
     */
    @Override
    public TransactionWithClosureError withStatus(TransactionStatusDto status) {
        return new TransactionWithClosureError(
                new TransactionWithCompletedAuthorization(
                        new TransactionWithRequestedAuthorization(
                                new TransactionActivated(
                                        this.getTransactionId(),
                                        new PaymentToken(this.getTransactionActivatedData().getPaymentToken()),
                                        this.getRptId(),
                                        this.getDescription(),
                                        this.getAmount(),
                                        this.getEmail(),
                                        this.getTransactionActivatedData().getFaultCode(),
                                        this.getTransactionActivatedData().getFaultCodeString(),
                                        this.getCreationDate(),
                                        status
                                ),
                                this.getTransactionAuthorizationRequestData()
                        ),
                        this.getTransactionAuthorizationStatusUpdateData()
                ),
                this.getEvent()
        );
    }
}
