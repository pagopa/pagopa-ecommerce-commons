package it.pagopa.ecommerce.commons.domain;

import it.pagopa.ecommerce.commons.documents.TransactionAuthorizationRequestedEvent;
import it.pagopa.ecommerce.commons.documents.TransactionAuthorizationStatusUpdatedEvent;
import it.pagopa.ecommerce.commons.documents.TransactionEvent;
import it.pagopa.ecommerce.commons.domain.pojos.BaseTransactionWithPaymentToken;
import it.pagopa.ecommerce.commons.domain.pojos.BaseTransactionWithRequestedAuthorization;
import it.pagopa.ecommerce.commons.generated.transactions.model.TransactionStatusDto;
import lombok.EqualsAndHashCode;

/**
 * <p>
 *     Transaction with a requested authorization.
 * </p>
 * <p>
 *     To this class you can apply an {@link TransactionAuthorizationStatusUpdatedEvent} to get a {@link TransactionWithCompletedAuthorization}
 * </p>
 *
 * @see Transaction
 * @see BaseTransactionWithRequestedAuthorization
 */
@EqualsAndHashCode(callSuper = true)
public final class TransactionWithRequestedAuthorization extends BaseTransactionWithRequestedAuthorization
        implements
        Transaction {

    public TransactionWithRequestedAuthorization(
            BaseTransactionWithPaymentToken transaction,
            TransactionAuthorizationRequestedEvent event
    ) {
        super(transaction, event.getData());
    }

    @Override
    public Transaction applyEvent(TransactionEvent<?> event) {
        if (event instanceof TransactionAuthorizationStatusUpdatedEvent authorizationStatusUpdatedEvent) {
            return new TransactionWithCompletedAuthorization(
                    this.withStatus(authorizationStatusUpdatedEvent.getData().getNewTransactionStatus()),
                    authorizationStatusUpdatedEvent
            );
        } else {
            return this;
        }
    }

    @Override
    public TransactionWithRequestedAuthorization withStatus(TransactionStatusDto status) {
        return new TransactionWithRequestedAuthorization(
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
        );
    }
}
