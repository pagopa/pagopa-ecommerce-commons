package it.pagopa.ecommerce.commons.domain;

import it.pagopa.ecommerce.commons.domain.pojos.BaseTransactionWithCompletedAuthorization;
import it.pagopa.ecommerce.commons.domain.pojos.BaseTransactionWithRequestedAuthorization;
import it.pagopa.ecommerce.commons.generated.events.v1.*;
import it.pagopa.ecommerce.commons.generated.transactions.model.TransactionStatusDto;
import lombok.EqualsAndHashCode;

/**
 * <p>
 *     Transaction with a completed authorization.
 * </p>
 * <p>
 *     To this class you can apply either a {@link TransactionClosureSentEvent} to get a {@link TransactionClosed}
 *     or a {@link TransactionClosureErrorEvent} to get a {@link TransactionWithClosureError}
 * </p>
 *
 * @see Transaction
 * @see BaseTransactionWithCompletedAuthorization
 */
@EqualsAndHashCode(callSuper = true)
public final class TransactionWithCompletedAuthorization extends BaseTransactionWithCompletedAuthorization
        implements
        Transaction {

    /**
     * Primary constructor
     *
     * @param baseTransaction base transaction
     * @param authorizationStatusUpdateData data related to authorization status update
     */
    public TransactionWithCompletedAuthorization(
            BaseTransactionWithRequestedAuthorization baseTransaction,
            TransactionAuthorizationStatusUpdateData authorizationStatusUpdateData
    ) {
        super(baseTransaction, authorizationStatusUpdateData);
    }

    @Override
    public Transaction applyEvent(Object event) {
        if (event instanceof TransactionClosureSentEvent closureSentEvent) {
            return new TransactionClosed(
                    this.withStatus(TransactionStatusDto.fromValue(closureSentEvent.getData().getNewTransactionStatus().value())),
                    closureSentEvent.getData()
            );
        } else if (event instanceof TransactionClosureErrorEvent closureErrorEvent) {
            return new TransactionWithClosureError(
                    this.withStatus(TransactionStatusDto.CLOSURE_ERROR),
                    closureErrorEvent
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
    public TransactionWithCompletedAuthorization withStatus(TransactionStatusDto status) {
        return new TransactionWithCompletedAuthorization(
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
        );
    }
}
