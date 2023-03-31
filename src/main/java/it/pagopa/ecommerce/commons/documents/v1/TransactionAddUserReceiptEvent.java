package it.pagopa.ecommerce.commons.documents.v1;

import it.pagopa.ecommerce.commons.domain.v1.TransactionEventCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Business event generated when requesting user receipt to be sent
 */
@Document(collection = "eventstore")
@NoArgsConstructor
@ToString(callSuper = true)
public final class TransactionAddUserReceiptEvent extends TransactionEvent<TransactionUserReceiptData> {

    /**
     * Convenience constructor which sets the creation date to now
     *
     * @param transactionId              transaction unique id
     * @param transactionUserReceiptData transaction user receipt event related data
     */
    public TransactionAddUserReceiptEvent(
            String transactionId,
            TransactionUserReceiptData transactionUserReceiptData
    ) {
        super(transactionId, TransactionEventCode.TRANSACTION_ADD_USER_RECEIPT_EVENT, transactionUserReceiptData);
    }
}
