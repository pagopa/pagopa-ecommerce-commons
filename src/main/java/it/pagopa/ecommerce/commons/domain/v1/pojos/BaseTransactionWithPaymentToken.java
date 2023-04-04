package it.pagopa.ecommerce.commons.domain.v1.pojos;

import it.pagopa.ecommerce.commons.documents.v1.TransactionActivatedData;
import it.pagopa.ecommerce.commons.documents.v1.TransactionActivatedEvent;
import it.pagopa.ecommerce.commons.domain.Confidential;
import it.pagopa.ecommerce.commons.domain.v1.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * <p>
 * POJO for an activated transaction. As the name implies, the only field added
 * is the payment token.
 * </p>
 * <p>
 * For simplicity we currently include all data associated to
 * {@link TransactionActivatedEvent TransactionActivatedEvent}.
 * </p>
 * <p>
 * To get the payment token, call
 * {@code BaseTransaction.getTransactionActivatedData().getPaymentToken()}
 * </p>
 *
 * @see BaseTransaction
 * @see TransactionActivatedEvent TransactionActivatedEvent
 */
@ToString
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public abstract class BaseTransactionWithPaymentToken extends BaseTransaction {

    TransactionActivatedData transactionActivatedData;

    /**
     * Constructor from base transaction
     *
     * @param baseTransaction          base transaction to be extended
     * @param transactionActivatedData data generated with the activation event
     */
    protected BaseTransactionWithPaymentToken(
            BaseTransaction baseTransaction,
            TransactionActivatedData transactionActivatedData
    ) {
        super(
                baseTransaction.getTransactionId(),
                transactionActivatedData.getPaymentNotices().stream()
                        .map(
                                noticeCode -> new PaymentNotice(
                                        new PaymentToken(noticeCode.getPaymentToken()),
                                        new RptId(noticeCode.getRptId()),
                                        new TransactionAmount(noticeCode.getAmount()),
                                        new TransactionDescription(noticeCode.getDescription()),
                                        new PaymentContextCode(noticeCode.getPaymentContextCode()),
                                        noticeCode.getTransferList().stream()
                                                .map(
                                                        tx -> new PaymentTransferInfo(
                                                                tx.getPaFiscalCode(),
                                                                tx.getDigitalStamp(),
                                                                tx.getTransferAmount(),
                                                                tx.getTransferCategory()
                                                        )
                                                ).toList()
                                )
                        ).toList(),
                baseTransaction.getEmail(),
                baseTransaction.getCreationDate(),
                baseTransaction.getClientId()
        );
        this.transactionActivatedData = transactionActivatedData;
    }

    /**
     * Constructors this {@link BaseTransactionWithPaymentToken} with the given
     * information
     *
     * @param transactionId            the transaction id
     * @param paymentNotices           a list of transaction payment notices
     * @param email                    the user email
     * @param creationDate             the transaction creation date
     * @param clientId                 the transaction originated client id
     * @param transactionActivatedData the transaction activated data
     */
    protected BaseTransactionWithPaymentToken(
            TransactionId transactionId,
            List<PaymentNotice> paymentNotices,
            Confidential<Email> email,
            ZonedDateTime creationDate,
            it.pagopa.ecommerce.commons.documents.v1.Transaction.ClientId clientId,
            TransactionActivatedData transactionActivatedData
    ) {
        super(
                transactionId,
                paymentNotices,
                email,
                creationDate,
                clientId
        );
        this.transactionActivatedData = transactionActivatedData;
    }
}
