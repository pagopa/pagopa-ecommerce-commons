package it.pagopa.ecommerce.commons.domain.v1;

import it.pagopa.ecommerce.commons.documents.PaymentTransferInformation;
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
 * Applicable events with resulting aggregates are:
 * <ul>
 * <li>{@link TransactionAuthorizationRequestedEvent} -->
 * {@link TransactionWithRequestedAuthorization}</li>
 * <li>{@link TransactionExpiredEvent} -->
 * {@link TransactionExpiredNotAuthorized}</li>
 * <li>{@link TransactionUserCanceledEvent} -->
 * {@link TransactionWithCancellationRequested}</li>
 * </ul>
 * Any other event than the above ones will be discarded.
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
     * @param transactionId               transaction id
     * @param paymentNotices              notice code list
     * @param email                       email where the payment receipt will be
     *                                    sent to
     * @param faultCode                   fault code generated during activation
     * @param faultCodeString             fault code auxiliary description
     * @param creationDate                creation date of this transaction
     * @param clientId                    a {@link ClientId} object
     * @param idCart                      the ec cart id
     * @param paymentTokenValidityTimeSec the payment token validity time in seconds
     */
    public TransactionActivated(
            TransactionId transactionId,
            List<PaymentNotice> paymentNotices,
            Confidential<Email> email,
            String faultCode,
            String faultCodeString,
            ZonedDateTime creationDate,
            ClientId clientId,
            String idCart,
            int paymentTokenValidityTimeSec
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
                                        n -> new it.pagopa.ecommerce.commons.documents.PaymentNotice(
                                                n.paymentToken().value(),
                                                n.rptId().value(),
                                                n.transactionDescription().value(),
                                                n.transactionAmount().value(),
                                                n.paymentContextCode().value(),
                                                n.transferList().stream()
                                                        .map(
                                                                tx -> new PaymentTransferInformation(
                                                                        tx.paFiscalCode(),
                                                                        tx.digitalStamp(),
                                                                        tx.transferAmount(),
                                                                        tx.transferCategory()
                                                                )
                                                        ).toList(),
                                                n.isAllCCP(),
                                                n.companyName().value(),
                                                n.creditorReferenceId()
                                        )
                                ).toList(),
                        faultCode,
                        faultCodeString,
                        clientId,
                        idCart,
                        paymentTokenValidityTimeSec
                )
        );
    }

    /**
     * Convenience constructor with creation date set to now.
     *
     * @param transactionId               transaction id
     * @param paymentNotices              notice code list
     * @param email                       email where the payment receipt will be
     *                                    sent to
     * @param faultCode                   fault code generated during activation
     * @param faultCodeString             fault code auxiliary description
     * @param clientId                    the origin from which the transaction
     *                                    started from
     * @param idCart                      the ec id cart
     * @param paymentTokenValidityTimeSec the payment token validity time in seconds
     */
    public TransactionActivated(
            TransactionId transactionId,
            List<PaymentNotice> paymentNotices,
            Confidential<Email> email,
            String faultCode,
            String faultCodeString,
            ClientId clientId,
            String idCart,
            int paymentTokenValidityTimeSec
    ) {
        this(
                transactionId,
                paymentNotices,
                email,
                faultCode,
                faultCodeString,
                ZonedDateTime.now(),
                clientId,
                idCart,
                paymentTokenValidityTimeSec
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
        if (event instanceof TransactionAuthorizationRequestedEvent transactionAuthorizationRequestedEvent) {
            return new TransactionWithRequestedAuthorization(
                    this,
                    transactionAuthorizationRequestedEvent.getData()
            );
        } else if (event instanceof TransactionExpiredEvent transactionExpiredEvent) {
            return new TransactionExpiredNotAuthorized(this, transactionExpiredEvent);
        } else if (event instanceof TransactionUserCanceledEvent transactionUserCanceledEvent) {
            return new TransactionWithCancellationRequested(this, transactionUserCanceledEvent);
        }
        return this;
    }
}
