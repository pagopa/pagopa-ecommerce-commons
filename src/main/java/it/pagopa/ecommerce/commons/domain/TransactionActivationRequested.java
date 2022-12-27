package it.pagopa.ecommerce.commons.domain;

import it.pagopa.ecommerce.commons.documents.TransactionActivatedEvent;
import it.pagopa.ecommerce.commons.domain.pojos.BaseTransaction;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.EqualsAndHashCode;

import java.time.ZonedDateTime;
import java.util.List;

import static java.time.ZonedDateTime.now;

/**
 * <p>
 * Transaction with an activation requested.
 * </p>
 * <p>
 * To this class you can apply an {@link TransactionActivatedEvent} to get a
 * {@link TransactionActivated}
 * </p>
 *
 * @see Transaction
 * @see BaseTransaction
 */
@EqualsAndHashCode(callSuper = true)
public final class TransactionActivationRequested extends BaseTransaction implements Transaction {

    /**
     * Primary constructor
     *
     * @param transactionId  transaction id
     * @param paymentNotices notice codes list
     * @param email          email where the payment receipt will be sent to
     * @param creationDate   creation date of this transaction
     * @param status         transaction status
     */
    public TransactionActivationRequested(
            TransactionId transactionId,
            List<PaymentNotice> paymentNotices,
            Email email,
            ZonedDateTime creationDate,
            TransactionStatusDto status
    ) {
        super(transactionId, paymentNotices, email, creationDate, status);
    }

    /**
     * Convenience constructor that sets the transaction creation date to now.
     *
     * @param transactionId  transaction id
     * @param paymentNotices notice codes list
     * @param email          email where the payment receipt will be sent to
     * @param status         transaction status
     */
    public TransactionActivationRequested(
            TransactionId transactionId,
            List<PaymentNotice> paymentNotices,
            Email email,
            TransactionStatusDto status
    ) {
        super(transactionId, paymentNotices, email, now(), status);
    }

    @Override
    public Transaction applyEvent(Object event) {
        if (event instanceof TransactionActivatedEvent transactionActivatedEvent) {
            return new TransactionActivated(this.withStatus(TransactionStatusDto.ACTIVATED), transactionActivatedEvent);
        } else {
            return this;
        }
    }

    /**
     * Change the transaction status
     *
     * @param status new status
     * @return a new transaction with the same data except for the status
     */
    @Override
    public TransactionActivationRequested withStatus(TransactionStatusDto status) {
        return new TransactionActivationRequested(
                this.getTransactionId(),
                this.getPaymentNotices(),
                this.getEmail(),
                this.getCreationDate(),
                status
        );
    }
}
