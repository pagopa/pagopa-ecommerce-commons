package it.pagopa.ecommerce.commons.domain;

import it.pagopa.ecommerce.commons.documents.TransactionActivatedEvent;
import it.pagopa.ecommerce.commons.documents.TransactionActivationRequestedEvent;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.EqualsAndHashCode;

import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

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
                event.getData().getNoticeCodes().stream()
                        .map(
                                n -> new NoticeCode(
                                        new PaymentToken(n.getPaymentToken()),
                                        new RptId(n.getRptId()),
                                        new TransactionAmount(n.getAmount()),
                                        new TransactionDescription(n.getDescription())
                                )
                        ).collect(Collectors.toList()),
                new Email(event.getData().getEmail()),
                event.getData().getFaultCode(),
                event.getData().getFaultCodeString(),
                ZonedDateTime.parse(event.getCreationDate()),
                TransactionStatusDto.ACTIVATED
        );
    }

    private TransactionActivationRequested applyActivationRequested(TransactionActivationRequestedEvent event) {
        return new TransactionActivationRequested(
                new TransactionId(UUID.fromString(event.getTransactionId())),
                event.getData().getNoticeCodes().stream()
                        .map(
                                n -> new NoticeCode(
                                        new PaymentToken(null),
                                        new RptId(n.getRptId()),
                                        new TransactionAmount(n.getAmount()),
                                        new TransactionDescription(n.getDescription())
                                )
                        ).collect(Collectors.toList()),
                new Email(event.getData().getEmail()),
                ZonedDateTime.parse(event.getCreationDate()),
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
