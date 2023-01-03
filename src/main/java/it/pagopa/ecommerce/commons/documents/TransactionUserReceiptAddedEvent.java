package it.pagopa.ecommerce.commons.documents;

import it.pagopa.ecommerce.commons.domain.TransactionEventCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Business event generated when sending a payment receipt to a user.
 */
@Document(collection = "eventstore")
@NoArgsConstructor
@ToString(callSuper = true)
public final class TransactionUserReceiptAddedEvent extends TransactionEvent<TransactionAddReceiptData> {

    /**
     * Convenience constructor which sets the creation date to now
     *
     * @param transactionId transaction unique id
     * @param data          event-specific data
     */
    public TransactionUserReceiptAddedEvent(
            String transactionId,
            TransactionAddReceiptData data
    ) {
        super(transactionId, TransactionEventCode.TRANSACTION_USER_RECEIPT_ADDED_EVENT, data);
    }
}
