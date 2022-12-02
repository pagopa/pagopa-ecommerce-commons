package it.pagopa.ecommerce.commons.domain;

import it.pagopa.ecommerce.commons.generated.events.v1.TransactionActivatedEvent;
import it.pagopa.ecommerce.commons.generated.events.v1.TransactionActivationRequestedEvent;
import it.pagopa.generated.transactions.server.model.TransactionStatusDto;
import lombok.EqualsAndHashCode;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * <p>
 * Empty transaction, meant as a starting point to which events can be applied
 * to.
 * </p>
 * <p>
 * To this class you can apply either an
 * {@link TransactionActivationRequestedEvent} (and get a
 * {@link TransactionActivationRequested} or a {@link TransactionActivatedEvent}
 * (and get a {@link TransactionActivated})
 * </p>
 *
 * @see Transaction
 */
@EqualsAndHashCode
public final class EmptyTransaction implements Transaction {
    private TransactionActivated applyActivation(TransactionActivatedEvent event) {
        return new TransactionActivated(
                new TransactionId(UUID.fromString(event.getTransactionId())),
                new PaymentToken(event.getPaymentToken()),
                new RptId(event.getRptId()),
                new TransactionDescription(event.getData().getDescription()),
                new TransactionAmount(event.getData().getAmount()),
                new Email(event.getData().getEmail()),
                event.getData().getFaultCode(),
                event.getData().getFaultCodeString(),
                ZonedDateTime.ofInstant(event.getCreationDate().toInstant(), ZoneId.systemDefault()),
                TransactionStatusDto.ACTIVATED
        );
    }

    private TransactionActivationRequested applyActivationRequested(TransactionActivationRequestedEvent event) {
        return new TransactionActivationRequested(
                new TransactionId(UUID.fromString(event.getTransactionId())),
                new RptId(event.getRptId()),
                new TransactionDescription(event.getData().getDescription()),
                new TransactionAmount(event.getData().getAmount()),
                new Email(event.getData().getEmail()),
                ZonedDateTime.ofInstant(event.getCreationDate().toInstant(), ZoneId.systemDefault()),
                TransactionStatusDto.ACTIVATION_REQUESTED
        );
    }

    @Override
	public Transaction applyEvent(Object event) {
		return switch (event) {
			case TransactionActivatedEvent transactionActivatedEvent -> this.applyActivation(transactionActivatedEvent);
			case TransactionActivationRequestedEvent transactionActivationRequestedEvent -> this.applyActivationRequested(transactionActivationRequestedEvent);
			case null, default -> this;
		};
	}
}
