package it.pagopa.ecommerce.commons.domain.v2.pojos;

import it.pagopa.ecommerce.commons.documents.v2.TransactionRefundedData;
import it.pagopa.ecommerce.commons.documents.v2.TransactionRefundedEvent;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * <p>
 * POJO for a refunded transaction.
 * </p>
 * <p>
 * All accrued data is exposed through the same structure generated by
 * {@link TransactionRefundedEvent TransactionRefundedEvent}, namely
 * {@link TransactionRefundedData TransactionRefundedData}.
 * </p>
 *
 * @see BaseTransactionWithRefundRequested
 * @see TransactionRefundedEvent
 */
@ToString
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public abstract class BaseTransactionRefunded extends BaseTransactionWithRefundRequested {
    TransactionRefundedData transactionRefundedData;

    /**
     * Primary constructor
     *
     * @param baseTransaction         base transaction with refund requested
     * @param transactionRefundedData the transaction refunded data
     */
    protected BaseTransactionRefunded(
            BaseTransactionWithRefundRequested baseTransaction,
            TransactionRefundedData transactionRefundedData
    ) {
        super(
                baseTransaction
        );
        this.transactionRefundedData = transactionRefundedData;
    }
}