package it.pagopa.ecommerce.commons.domain.v1;

import it.pagopa.ecommerce.commons.documents.v1.Transaction.ClientId;
import it.pagopa.ecommerce.commons.documents.v1.TransactionActivatedData;
import it.pagopa.ecommerce.commons.documents.v1.TransactionAuthorizationRequestedEvent;
import it.pagopa.ecommerce.commons.documents.v1.TransactionExpiredEvent;
import it.pagopa.ecommerce.commons.documents.v1.TransactionUserCanceledEvent;
import it.pagopa.ecommerce.commons.domain.Confidential;
import it.pagopa.ecommerce.commons.domain.v1.pojos.BaseTransactionWithPaymentToken;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.ZonedDateTime;
import java.util.List;

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
@ToString
public final class TransactionActivated extends BaseTransactionWithPaymentToken implements Transaction {

    /**
     * Primary constructor
     *
     * @param transactionId   transaction id
     * @param paymentNotices  notice code list
     * @param email           email where the payment receipt will be sent to
     * @param faultCode       fault code generated during activation
     * @param faultCodeString fault code auxiliary description
     * @param creationDate    creation date of this transaction
     * @param clientId        a {@link ClientId} object
     */
    public TransactionActivated(
            TransactionId transactionId,
            List<PaymentNotice> paymentNotices,
            Confidential<Email> email,
            String faultCode,
            String faultCodeString,
            ZonedDateTime creationDate,
            ClientId clientId
    ) {
        super(

                transactionId,
                paymentNotices,
                email,
                creationDate,
                clientId,

                new TransactionActivatedData(
                        email,
                        paymentNotices.stream()
                                .map(
                                        n -> new it.pagopa.ecommerce.commons.documents.v1.PaymentNotice(
                                                n.paymentToken().value(),
                                                n.rptId().value(),
                                                n.transactionDescription().value(),
                                                n.transactionAmount().value(),
                                                n.paymentContextCode().value()
                                        )
                                ).toList(),
                        faultCode,
                        faultCodeString,
                        clientId
                )
        );
    }

    /**
     * Convenience constructor with creation date set to now.
     *
     * @param transactionId   transaction id
     * @param paymentNotices  notice code list
     * @param email           email where the payment receipt will be sent to
     * @param faultCode       fault code generated during activation
     * @param faultCodeString fault code auxiliary description
     * @param clientId        the origin from which the transaction started from
     */
    public TransactionActivated(
            TransactionId transactionId,
            List<PaymentNotice> paymentNotices,
            Confidential<Email> email,
            String faultCode,
            String faultCodeString,
            ClientId clientId
    ) {
        this(
                transactionId,
                paymentNotices,
                email,
                faultCode,
                faultCodeString,
                ZonedDateTime.now(),
                clientId
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TransactionStatusDto getStatus() {
        return TransactionStatusDto.ACTIVATED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Transaction applyEvent(Object event) {
        return switch (event) {
            case TransactionAuthorizationRequestedEvent transactionAuthorizationRequestedEvent ->
                    new TransactionWithRequestedAuthorization(
                            this,
                            transactionAuthorizationRequestedEvent.getData()
                    );
            case TransactionExpiredEvent transactionExpiredEvent ->
                    new TransactionExpiredNotAuthorized(this, transactionExpiredEvent);
            case TransactionUserCanceledEvent transactionUserCanceledEvent ->
                    new TransactionUserCanceled(this, transactionUserCanceledEvent);
            default -> this;
        };
    }
}
