package it.pagopa.ecommerce.commons.domain;

import it.pagopa.ecommerce.commons.documents.TransactionActivatedEvent;
import it.pagopa.ecommerce.commons.documents.TransactionActivationRequestedEvent;
import it.pagopa.ecommerce.commons.documents.TransactionEvent;
import it.pagopa.ecommerce.commons.generated.transactions.model.TransactionStatusDto;
import lombok.EqualsAndHashCode;

import java.time.ZonedDateTime;
import java.util.UUID;

@EqualsAndHashCode
public final class EmptyTransaction implements Transaction {
    public TransactionActivated applyActivation(TransactionActivatedEvent event) {
        return new TransactionActivated(
                new TransactionId(UUID.fromString(event.getTransactionId())),
                new PaymentToken(event.getPaymentToken()),
                new RptId(event.getRptId()),
                new TransactionDescription(event.getData().getDescription()),
                new TransactionAmount(event.getData().getAmount()),
                new Email(event.getData().getEmail()),
                event.getData().getFaultCode(),
                event.getData().getFaultCodeString(),
                ZonedDateTime.parse(event.getCreationDate()),
                TransactionStatusDto.ACTIVATED
        );
    }

    public TransactionActivationRequested applyActivationRequested(TransactionActivationRequestedEvent event) {
        return new TransactionActivationRequested(
                new TransactionId(UUID.fromString(event.getTransactionId())),
                new RptId(event.getRptId()),
                new TransactionDescription(event.getData().getDescription()),
                new TransactionAmount(event.getData().getAmount()),
                new Email(event.getData().getEmail()),
                ZonedDateTime.parse(event.getCreationDate()),
                TransactionStatusDto.ACTIVATION_REQUESTED
        );
    }

    @Override
	public Transaction applyEvent(TransactionEvent<?> event) {
		return switch (event) {
			case TransactionActivatedEvent transactionActivatedEvent -> this.applyActivation(transactionActivatedEvent);
			case TransactionActivationRequestedEvent transactionActivationRequestedEvent -> this.applyActivationRequested(transactionActivationRequestedEvent);
			case null, default -> this;
		};
	}
}
