package it.pagopa.ecommerce.commons.domain.v2;

import it.pagopa.ecommerce.commons.documents.PaymentTransferInformation;
import it.pagopa.ecommerce.commons.documents.v2.Transaction.ClientId;
import it.pagopa.ecommerce.commons.documents.v2.TransactionActivatedData;
import it.pagopa.ecommerce.commons.documents.v2.TransactionAuthorizationRequestedEvent;
import it.pagopa.ecommerce.commons.documents.v2.TransactionExpiredEvent;
import it.pagopa.ecommerce.commons.documents.v2.TransactionUserCanceledEvent;
import it.pagopa.ecommerce.commons.documents.v2.activation.TransactionGatewayActivationData;
import it.pagopa.ecommerce.commons.domain.v2.pojos.BaseTransactionWithPaymentToken;
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
     * @param transactionId                    transaction id
     * @param paymentNotices                   notice code list
     * @param email                            email where the payment receipt will
     *                                         be sent to
     * @param faultCode                        fault code generated during
     *                                         activation
     * @param faultCodeString                  fault code auxiliary description
     * @param creationDate                     creation date of this transaction
     * @param clientId                         a {@link ClientId} object
     * @param idCart                           the ec cart id
     * @param paymentTokenValidityTimeSec      the payment token validity time in
     *                                         seconds
     * @param transactionGatewayActivationData transaction activation data
     * @param userId                           the user unique id
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
            int paymentTokenValidityTimeSec,
            TransactionGatewayActivationData transactionGatewayActivationData,
            String userId
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
                        paymentTokenValidityTimeSec,
                        transactionGatewayActivationData,
                        userId
                )
        );
    }

    /**
     * Convenience constructor with creation date set to now.
     *
     * @param transactionId                    transaction id
     * @param paymentNotices                   notice code list
     * @param email                            email where the payment receipt will
     *                                         be sent to
     * @param faultCode                        fault code generated during
     *                                         activation
     * @param faultCodeString                  fault code auxiliary description
     * @param clientId                         the origin from which the transaction
     *                                         started from
     * @param idCart                           the ec id cart
     * @param paymentTokenValidityTimeSec      the payment token validity time in
     *                                         seconds
     * @param transactionGatewayActivationData transaction activation data
     * @param userId                           the user unique id
     */
    public TransactionActivated(
            TransactionId transactionId,
            List<PaymentNotice> paymentNotices,
            Confidential<Email> email,
            String faultCode,
            String faultCodeString,
            ClientId clientId,
            String idCart,
            int paymentTokenValidityTimeSec,
            TransactionGatewayActivationData transactionGatewayActivationData,
            String userId
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
                paymentTokenValidityTimeSec,
                transactionGatewayActivationData,
                userId
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
                    new TransactionWithCancellationRequested(this, transactionUserCanceledEvent);
            default -> this;
        };
    }
}
