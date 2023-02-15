package it.pagopa.ecommerce.commons.domain;

import it.pagopa.ecommerce.commons.documents.TransactionActivatedEvent;
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
 * To this class you can apply a
 * {@link it.pagopa.ecommerce.commons.documents.TransactionActivatedEvent} (and
 * get a {@link it.pagopa.ecommerce.commons.domain.TransactionActivated})
 * </p>
 *
 * @see Transaction
 */
@EqualsAndHashCode
public final class EmptyTransaction implements Transaction {
    private TransactionActivated applyActivation(TransactionActivatedEvent event) {
        return new TransactionActivated(
                new TransactionId(UUID.fromString(event.getTransactionId())),
                event.getData().getPaymentNotices().stream()
                        .map(
                                n -> new PaymentNotice(
                                        new PaymentToken(n.getPaymentToken()),
                                        new RptId(n.getRptId()),
                                        new TransactionAmount(n.getAmount()),
                                        new TransactionDescription(n.getDescription()),
                                        new PaymentContextCode(n.getPaymentContextCode())
                                )
                        ).collect(Collectors.toList()),
                new Email(event.getData().getEmail()),
                event.getData().getFaultCode(),
                event.getData().getFaultCodeString(),
                ZonedDateTime.parse(event.getCreationDate()),
                event.getData().getClientId()
        );
    }

    /** {@inheritDoc} */
    @Override
	public Transaction applyEvent(Object event) {
		return switch (event) {
			case TransactionActivatedEvent transactionActivatedEvent -> this.applyActivation(transactionActivatedEvent);
			case null, default -> this;
		};
	}
}
