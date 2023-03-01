package it.pagopa.ecommerce.commons.domain.v1.pojos;

import it.pagopa.ecommerce.commons.documents.v1.TransactionUserReceiptData;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * <p>
 * POJO for a transaction with user receipt
 * </p>
 *
 * @see BaseTransactionClosed
 * @see TransactionUserReceiptData
 */
@ToString
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
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
