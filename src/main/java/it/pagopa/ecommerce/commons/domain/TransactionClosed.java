package it.pagopa.ecommerce.commons.domain;

import it.pagopa.ecommerce.commons.documents.TransactionAuthorizationRequestedEvent;
import it.pagopa.ecommerce.commons.documents.TransactionAuthorizationStatusUpdatedEvent;
import it.pagopa.ecommerce.commons.documents.TransactionClosureSentEvent;
import it.pagopa.ecommerce.commons.documents.TransactionEvent;
import it.pagopa.ecommerce.commons.domain.pojos.BaseTransactionClosed;
import it.pagopa.ecommerce.commons.domain.pojos.BaseTransactionWithCompletedAuthorization;
import it.pagopa.ecommerce.commons.generated.transactions.model.TransactionStatusDto;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class TransactionClosed extends BaseTransactionClosed implements Transaction {

    public TransactionClosed(
            BaseTransactionWithCompletedAuthorization baseTransaction,
            TransactionClosureSentEvent event
    ) {
        super(baseTransaction, event.getData());
    }

    @Override
    public Transaction applyEvent(TransactionEvent<?> event) {
        return this;
    }

    @Override
    public TransactionClosed withStatus(TransactionStatusDto status) {
        return new TransactionClosed(
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
                ),
                new TransactionClosureSentEvent(
                        this.getTransactionId().value().toString(),
                        this.getRptId().value(),
                        this.getTransactionActivatedData().getPaymentToken(),
                        this.getTransactionClosureSendData()
                )
        );
    }
}
