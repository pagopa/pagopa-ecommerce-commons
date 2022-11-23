package it.pagopa.ecommerce.commons.domain;

import static java.time.ZonedDateTime.now;

import it.pagopa.ecommerce.commons.documents.TransactionActivatedEvent;
import it.pagopa.ecommerce.commons.documents.TransactionEvent;
import it.pagopa.ecommerce.commons.domain.pojos.BaseTransaction;
import it.pagopa.ecommerce.commons.generated.transactions.model.TransactionStatusDto;
import java.time.ZonedDateTime;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class TransactionActivationRequested extends BaseTransaction implements Transaction {

    public TransactionActivationRequested(
            TransactionId transactionId,
            RptId rptId,
            TransactionDescription description,
            TransactionAmount amount,
            Email email,
            ZonedDateTime creationDate,
            TransactionStatusDto status
    ) {
        super(transactionId, rptId, description, amount, email, creationDate, status);
    }

    public TransactionActivationRequested(
            TransactionId transactionId,
            RptId rptId,
            TransactionDescription description,
            TransactionAmount amount,
            Email email,
            TransactionStatusDto status
    ) {
        super(transactionId, rptId, description, amount, email, now(), status);
    }

    @Override
    public Transaction applyEvent(TransactionEvent<?> event) {
        if (event instanceof TransactionActivatedEvent transactionActivatedEvent) {
            return new TransactionActivated(this.withStatus(TransactionStatusDto.ACTIVATED), transactionActivatedEvent);
        } else {
            return this;
        }
    }

    @Override
    public TransactionActivationRequested withStatus(TransactionStatusDto status) {
        return new TransactionActivationRequested(
                this.getTransactionId(),
                this.getRptId(),
                this.getDescription(),
                this.getAmount(),
                this.getEmail(),
                this.getCreationDate(),
                status
        );
    }
}
