package it.pagopa.ecommerce.commons.domain;

import it.pagopa.ecommerce.commons.documents.*;
import it.pagopa.ecommerce.commons.domain.pojos.BaseTransactionWithCompletedAuthorization;
import it.pagopa.ecommerce.commons.domain.pojos.BaseTransactionWithPaymentToken;
import it.pagopa.ecommerce.commons.domain.pojos.BaseTransactionWithRequestedAuthorization;
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

    public TransactionWithCompletedAuthorization(
            BaseTransactionWithRequestedAuthorization baseTransaction,
            TransactionAuthorizationStatusUpdatedEvent event
    ) {
        super(baseTransaction, event.getData());
    }

    @Override
    public Transaction applyEvent(TransactionEvent<?> event) {
        if (event instanceof TransactionClosureSentEvent closureSentEvent) {
            return new TransactionClosed(
                    this.withStatus(closureSentEvent.getData().getNewTransactionStatus()),
                    closureSentEvent
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
                        new TransactionAuthorizationRequestedEvent(
                                this.getTransactionId().value().toString(),
                                this.getRptId().value(),
                                this.getTransactionActivatedData().getPaymentToken(),
                                this.getTransactionAuthorizationRequestData()
                        )
                ),
                new TransactionAuthorizationStatusUpdatedEvent(
                        this.getTransactionId().value().toString(),
                        this.getRptId().value(),
                        this.getTransactionActivatedData().getPaymentToken(),
                        this.getTransactionAuthorizationStatusUpdateData()
                )
        );
    }
}
