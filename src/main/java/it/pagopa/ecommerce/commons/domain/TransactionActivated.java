package it.pagopa.ecommerce.commons.domain;

import it.pagopa.ecommerce.commons.documents.Transaction.ClientId;
import it.pagopa.ecommerce.commons.documents.TransactionActivatedData;
import it.pagopa.ecommerce.commons.documents.TransactionAuthorizationRequestedEvent;
import it.pagopa.ecommerce.commons.documents.TransactionExpiredEvent;
import it.pagopa.ecommerce.commons.documents.TransactionUserCanceledEvent;
import it.pagopa.ecommerce.commons.domain.pojos.BaseTransactionWithPaymentToken;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.EqualsAndHashCode;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * <p>
 * Activated transaction.
 * </p>
 * <p>
 * To this class you can apply an
 * {@link it.pagopa.ecommerce.commons.documents.TransactionAuthorizationRequestedEvent}
 * to get a
 * {@link it.pagopa.ecommerce.commons.domain.TransactionWithRequestedAuthorization}
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
            Email email,
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
                        email.value(),
                        paymentNotices.stream()
                                .map(
                                        n -> new it.pagopa.ecommerce.commons.documents.PaymentNotice(
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
            Email email,
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
            case TransactionExpiredEvent transactionExpiredEvent -> new TransactionExpiredNotAuthorized(this);
            case TransactionUserCanceledEvent transactionUserCanceledEvent -> new TransactionUserCanceled(this);
            default -> this;
        };
    }
}
