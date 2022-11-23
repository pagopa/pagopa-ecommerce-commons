package it.pagopa.ecommerce.commons.domain;

import it.pagopa.ecommerce.commons.documents.TransactionActivatedData;
import it.pagopa.ecommerce.commons.documents.TransactionActivatedEvent;
import it.pagopa.ecommerce.commons.documents.TransactionAuthorizationRequestedEvent;
import it.pagopa.ecommerce.commons.documents.TransactionEvent;
import it.pagopa.ecommerce.commons.domain.pojos.BaseTransactionWithPaymentToken;
import it.pagopa.ecommerce.commons.generated.transactions.model.TransactionStatusDto;
import java.time.ZonedDateTime;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class TransactionActivated extends BaseTransactionWithPaymentToken implements Transaction {

    public TransactionActivated(
            TransactionId transactionId,
            PaymentToken paymentToken,
            RptId rptId,
            TransactionDescription description,
            TransactionAmount amount,
            Email email,
            String faultCode,
            String faultCodeString,
            ZonedDateTime creationDate,
            TransactionStatusDto status
    ) {
        super(
                new TransactionActivationRequested(
                        transactionId,
                        rptId,
                        description,
                        amount,
                        email,
                        creationDate,
                        status
                ),
                new TransactionActivatedData(
                        description.value(),
                        amount.value(),
                        email.value(),
                        faultCode,
                        faultCodeString,
                        paymentToken.value()
                )
        );
    }

    public TransactionActivated(
            TransactionId transactionId,
            PaymentToken paymentToken,
            RptId rptId,
            TransactionDescription description,
            TransactionAmount amount,
            Email email,
            String faultCode,
            String faultCodeString,
            TransactionStatusDto status
    ) {
        this(
                transactionId,
                paymentToken,
                rptId,
                description,
                amount,
                email,
                faultCode,
                faultCodeString,
                ZonedDateTime.now(),
                status
        );
    }

    public TransactionActivated(
            TransactionActivationRequested transactionActivationRequested,
            TransactionActivatedEvent event
    ) {
        super(transactionActivationRequested, event.getData());
    }

    @Override
    public Transaction applyEvent(TransactionEvent<?> event) {
        if (event instanceof TransactionAuthorizationRequestedEvent) {
            return new TransactionWithRequestedAuthorization(this, (TransactionAuthorizationRequestedEvent) event);
        } else {
            return this;
        }
    }

    @Override
    public TransactionActivated withStatus(TransactionStatusDto status) {
        return new TransactionActivated(
                this.getTransactionId(),
                new PaymentToken(this.getTransactionActivatedData().getPaymentToken()),
                this.getRptId(),
                this.getDescription(),
                this.getAmount(),
                this.getEmail(),
                this.getTransactionActivatedData().getFaultCode(),
                this.getTransactionActivatedData().getFaultCodeString(),
                this.getCreationDate(),
                this.getStatus()
        );
    }
}
