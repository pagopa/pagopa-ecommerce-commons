package it.pagopa.ecommerce.commons.domain.v1.pojos;

import it.pagopa.ecommerce.commons.documents.v1.TransactionExpiredData;
import it.pagopa.ecommerce.commons.documents.v1.TransactionExpiredEvent;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * <p>
 * POJO for an expired transaction.
 * </p>
 * <p>
 * All accrued data is exposed through the same structure generated by
 * {@link TransactionExpiredEvent TransactionExpiredEvent}, namely
 * {@link TransactionExpiredData TransactionExpiredData}.
 * </p>
 *
 * @see BaseTransaction
 * @see TransactionExpiredEvent
 */
@ToString
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public abstract class BaseTransactionExpired extends BaseTransaction {
    TransactionExpiredData transactionExpiredData;
    BaseTransaction transactionAtPreviousState;

    /**
     * Primary constructor
     *
     * @param baseTransaction        base transaction
     * @param transactionExpiredData transaction expiration data
     */
    protected BaseTransactionExpired(
            BaseTransaction baseTransaction,
            TransactionExpiredData transactionExpiredData
    ) {
        super(
                baseTransaction.getTransactionId(),
                baseTransaction.getPaymentNotices(),
                baseTransaction.getEmail(),
                baseTransaction.getCreationDate(),
                baseTransaction.getClientId()
        );

        this.transactionExpiredData = transactionExpiredData;
        this.transactionAtPreviousState = baseTransaction;
    }
}