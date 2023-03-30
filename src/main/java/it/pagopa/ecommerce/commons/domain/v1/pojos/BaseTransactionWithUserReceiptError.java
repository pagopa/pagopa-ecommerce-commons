package it.pagopa.ecommerce.commons.domain.v1.pojos;

import it.pagopa.ecommerce.commons.documents.v1.TransactionUserReceiptData;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * <p>
 * POJO for a transaction with an error adding user receipt
 * </p>
 *
 * @see BaseTransactionWithRequestedUserReceipt
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
 * Suppressed because the Transaction hierarchy modeled here force BaseTransactionWithUserReceipt
 * to be instantiated only starting from a TransactionClosed. The hierarchy dept is strictly correlated
 * to the depth of the graph representing the finite state machine so can be accepted that hierarchy level
 * is deeper than the max authorized level
 *
 * @formatter:on
 */
@SuppressWarnings("java:S110")
public abstract class BaseTransactionWithUserReceiptError extends BaseTransactionWithRequestedUserReceipt {
    TransactionUserReceiptData transactionUserReceiptData;

    /**
     * Primary constructor
     *
     * @param baseTransaction            base transaction
     * @param transactionUserReceiptData transaction user receipt data
     */
    protected BaseTransactionWithUserReceiptError(
            BaseTransactionWithRequestedUserReceipt baseTransaction,
            TransactionUserReceiptData transactionUserReceiptData
    ) {
        super(
                baseTransaction,
                transactionUserReceiptData
        );
        this.transactionUserReceiptData = transactionUserReceiptData;
    }
}
