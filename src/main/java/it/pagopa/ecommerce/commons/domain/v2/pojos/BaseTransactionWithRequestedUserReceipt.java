package it.pagopa.ecommerce.commons.domain.v2.pojos;

import it.pagopa.ecommerce.commons.documents.v2.TransactionUserReceiptData;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * <p>
 * POJO for a transaction for which has been requested to add an user receipt
 * </p>
 *
 * @see BaseTransactionClosed
 * @see TransactionUserReceiptData
 */
@ToString
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
/*
 * @formatter:off
 *
 * Warning java:S110 - This class has x parents which is greater than 5 authorized
 * Suppressed because the Transaction hierarchy modeled here force BaseTransactionWithRequestedUserReceipt
 * to be instantiated only starting from a TransactionClosed. The hierarchy dept is strictly correlated
 * to the depth of the graph representing the finite state machine so can be accepted that hierarchy level
 * is deeper than the max authorized level
 *
 * @formatter:on
 */
@SuppressWarnings("java:S110")
public abstract class BaseTransactionWithRequestedUserReceipt extends BaseTransactionClosed {
    TransactionUserReceiptData transactionUserReceiptData;

    /**
     * Primary constructor
     *
     * @param baseTransaction            base transaction
     * @param transactionUserReceiptData transaction user receipt data
     */
    protected BaseTransactionWithRequestedUserReceipt(
            BaseTransactionClosed baseTransaction,
            TransactionUserReceiptData transactionUserReceiptData
    ) {
        super(
                baseTransaction,
                baseTransaction.getTransactionClosureData()
        );
        this.transactionUserReceiptData = transactionUserReceiptData;
    }
}
