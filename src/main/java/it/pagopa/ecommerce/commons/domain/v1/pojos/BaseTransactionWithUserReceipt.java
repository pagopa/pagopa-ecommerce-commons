package it.pagopa.ecommerce.commons.domain.v1.pojos;

import it.pagopa.ecommerce.commons.documents.v1.TransactionClosureData;
import it.pagopa.ecommerce.commons.documents.v1.TransactionUserReceiptData;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * <p>
 * POJO for a transaction with a user receipt requested
 * </p>
 *
 * @see BaseTransactionClosed
 * @see TransactionClosureData
 */
@ToString
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
/*
 * @formatter:off
 *
 * Warning java:S110 - This class has x parents which is greater than 5 authorized
 * Suppressed because the Transaction hierarchy modeled here force TransactionWithUserReceiptKo
 * to be instantiated only starting from a TransactionClosed. The hierarchy dept is strictly correlated
 * to the depth of the graph representing the finite state machine so can be accepted that hierarchy level
 * is deeper than the max authorized level
 *
 * @formatter:on
 */
@SuppressWarnings("java:S110")
public abstract class BaseTransactionWithUserReceipt extends BaseTransactionClosed {
    TransactionUserReceiptData transactionUserReceiptData;

    /**
     * Primary constructor
     *
     * @param baseTransaction            base transaction
     * @param transactionUserReceiptData transaction user receipt data
     */
    protected BaseTransactionWithUserReceipt(
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
