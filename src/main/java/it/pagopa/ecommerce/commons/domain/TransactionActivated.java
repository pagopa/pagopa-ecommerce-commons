package it.pagopa.ecommerce.commons.domain;

import it.pagopa.ecommerce.commons.documents.TransactionActivatedData;
import it.pagopa.ecommerce.commons.documents.TransactionActivatedEvent;
import it.pagopa.ecommerce.commons.documents.TransactionAuthorizationRequestedEvent;
import it.pagopa.ecommerce.commons.domain.pojos.BaseTransactionWithPaymentToken;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.EqualsAndHashCode;

import java.time.ZonedDateTime;

/**
 * <p>
 * Activated transaction.
 * </p>
 * <p>
 * To this class you can apply an {@link TransactionAuthorizationRequestedEvent}
 * to get a {@link TransactionWithRequestedAuthorization}
 * </p>
 *
 * @see Transaction
 * @see BaseTransactionWithPaymentToken
 */
@EqualsAndHashCode(callSuper = true)
public final class TransactionActivated extends BaseTransactionWithPaymentToken implements Transaction {

    /**
     * Primary constructor
     *
     * @param transactionId   transaction id
     * @param paymentToken    payment token
     * @param rptId           RPT id
     * @param description     payment description
     * @param amount          transaction amount in euro cents
     * @param email           email where the payment receipt will be sent to
     * @param faultCode       fault code generated during activation
     * @param faultCodeString fault code auxiliary description
     * @param creationDate    creation date of this transaction
     * @param status          transaction status
     */
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

    /**
     * Convenience constructor with creation date set to now.
     *
     * @param transactionId   transaction id
     * @param paymentToken    payment token
     * @param rptId           RPT id
     * @param description     payment description
     * @param amount          transaction amount in euro cents
     * @param email           email where the payment receipt will be sent to
     * @param faultCode       fault code generated during activation
     * @param faultCodeString fault code auxiliary description
     * @param status          transaction status
     */
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

    /**
     * Conversion constructor to construct an activated transaction from a
     * {@link TransactionActivationRequested}
     *
     * @param transactionActivationRequested transaction
     * @param event                          activation event
     */
    public TransactionActivated(
            TransactionActivationRequested transactionActivationRequested,
            TransactionActivatedEvent event
    ) {
        super(transactionActivationRequested, event.getData());
    }

    @Override
    public Transaction applyEvent(Object event) {
        if (event instanceof TransactionAuthorizationRequestedEvent transactionAuthorizationRequestedEvent) {
            return new TransactionWithRequestedAuthorization(
                    this.withStatus(TransactionStatusDto.AUTHORIZATION_REQUESTED),
                    transactionAuthorizationRequestedEvent.getData()
            );
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
                status
        );
    }
}
