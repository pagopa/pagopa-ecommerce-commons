package it.pagopa.ecommerce.commons.documents.v2;

import it.pagopa.ecommerce.commons.domain.v2.TransactionEventCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Business event generated when requesting user receipt to be sent
 */
@Document(collection = "eventstore")
@NoArgsConstructor
@ToString(callSuper = true)
public final class TransactionUserReceiptRequestedEvent extends TransactionEvent<TransactionUserReceiptData> {

    /**
     * Convenience constructor which sets the creation date to now
     *
     * @param transactionId              transaction unique id
     * @param transactionUserReceiptData transaction user receipt event related data
     */
    public TransactionUserReceiptRequestedEvent(
            String transactionId,
            TransactionUserReceiptData transactionUserReceiptData
    ) {
        super(transactionId, TransactionEventCode.TRANSACTION_USER_RECEIPT_REQUESTED_EVENT, transactionUserReceiptData);
    }
}
