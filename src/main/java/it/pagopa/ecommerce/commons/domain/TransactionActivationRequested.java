package it.pagopa.ecommerce.commons.domain;

import it.pagopa.ecommerce.commons.documents.TransactionActivatedEvent;
import it.pagopa.ecommerce.commons.domain.pojos.BaseTransaction;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import it.pagopa.ecommerce.commons.documents.Transaction.ClientId;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.List;

import static java.time.ZonedDateTime.now;

/**
 * <p>
 * Transaction with an activation requested.
 * </p>
 * <p>
 * To this class you can apply an
 * {@link it.pagopa.ecommerce.commons.documents.TransactionActivatedEvent} to
 * get a {@link it.pagopa.ecommerce.commons.domain.TransactionActivated}
 * </p>
 *
 * @see Transaction
 * @see BaseTransaction
 */
@EqualsAndHashCode(callSuper = true)
@Getter
public final class TransactionActivationRequested extends BaseTransaction implements Transaction {

    /**
     * Primary constructor
     *
     * @param transactionId  transaction id
     * @param paymentNotices notice codes list
     * @param email          email where the payment receipt will be sent to
     * @param creationDate   creation date of this transaction
     * @param status         transaction status
     * @param clientId       the origin from which the transaction started from
     */
    public TransactionActivationRequested(
            TransactionId transactionId,
            List<PaymentNotice> paymentNotices,
            Email email,
            ZonedDateTime creationDate,
            TransactionStatusDto status,
            ClientId clientId
    ) {
        super(transactionId, paymentNotices, email, creationDate, status, clientId);
    }

    /**
     * Convenience constructor that sets the transaction creation date to now.
     *
     * @param transactionId  transaction id
     * @param paymentNotices notice codes list
     * @param email          email where the payment receipt will be sent to
     * @param status         transaction status
     * @param clientId       the origin from which the transaction started from
     */
    public TransactionActivationRequested(
            TransactionId transactionId,
            List<PaymentNotice> paymentNotices,
            Email email,
            TransactionStatusDto status,
            ClientId clientId
    ) {
        super(transactionId, paymentNotices, email, now(), status, clientId);
    }

    /** {@inheritDoc} */
    @Override
    public Transaction applyEvent(Object event) {
        if (event instanceof TransactionActivatedEvent transactionActivatedEvent) {
            return new TransactionActivated(this.withStatus(TransactionStatusDto.ACTIVATED), transactionActivatedEvent);
        } else {
            return this;
        }
    }

    /**
     * {@inheritDoc}
     *
     * Change the transaction status
     */
    @Override
    public TransactionActivationRequested withStatus(TransactionStatusDto status) {
        return new TransactionActivationRequested(
                this.getTransactionId(),
                this.getPaymentNotices(),
                this.getEmail(),
                this.getCreationDate(),
                status,
                this.getClientId()
        );
    }
}
