package it.pagopa.ecommerce.commons.domain.v1;

import it.pagopa.ecommerce.commons.documents.v1.TransactionActivatedEvent;
import lombok.EqualsAndHashCode;

import java.time.ZonedDateTime;
import java.util.stream.Collectors;

/**
 * <p>
 * Empty transaction, meant as a starting point to which events can be applied
 * to.
 * </p>
 * <p>
 * To this class you can apply a {@link TransactionActivatedEvent} (and get a
 * {@link TransactionActivated})
 * </p>
 *
 * @see Transaction
 */
@EqualsAndHashCode
public final class EmptyTransaction implements Transaction {
    private TransactionActivated applyActivation(TransactionActivatedEvent event) {
        return new TransactionActivated(
                new TransactionId(event.getTransactionId()),
                event.getData().getPaymentNotices().stream()
                        .map(
                                n -> new PaymentNotice(
                                        new PaymentToken(n.getPaymentToken()),
                                        new RptId(n.getRptId()),
                                        new TransactionAmount(n.getAmount()),
                                        new TransactionDescription(n.getDescription()),
                                        new PaymentContextCode(n.getPaymentContextCode()),
                                        n.getTransferList().stream()
                                                .map(
                                                        tx -> new PaymentTransferInfo(
                                                                tx.getPaFiscalCode(),
                                                                tx.getDigitalStamp(),
                                                                tx.getTransferAmount(),
                                                                tx.getTransferCategory()
                                                        )
                                                ).toList(),
                                        n.isAllCCP(),
                                        new CompanyName(n.getCompanyName()),
                                        n.getCreditorReferenceId()
                                )
                        ).collect(Collectors.toList()),
                event.getData().getEmail(),
                event.getData().getFaultCode(),
                event.getData().getFaultCodeString(),
                ZonedDateTime.parse(event.getCreationDate()),
                event.getData().getClientId(),
                event.getData().getIdCart(),
                event.getData().getPaymentTokenValiditySeconds()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Transaction applyEvent(Object event) {
        if (event instanceof TransactionActivatedEvent transactionActivatedEvent) {
            return this.applyActivation(transactionActivatedEvent);
        }
        return this;
    }
}
