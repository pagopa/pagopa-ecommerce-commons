package it.pagopa.ecommerce.commons.documents.v2;

import it.pagopa.ecommerce.commons.domain.v2.TransactionEventCode;
import lombok.Generated;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Business event corresponding to a retry of add user receipt . This event maps
 * to a transient state for transactions.
 */
@Document(collection = "eventstore")
@Generated
@NoArgsConstructor
@ToString(callSuper = true)
public final class TransactionUserReceiptAddRetriedEvent extends TransactionEvent<TransactionRetriedData> {

    /**
     * Convenience constructor which sets the creation date to now
     *
     * @param transactionId          transaction unique id
     * @param transactionRetriedData retry count data
     */
    public TransactionUserReceiptAddRetriedEvent(
            String transactionId,
            TransactionRetriedData transactionRetriedData
    ) {
        super(transactionId, TransactionEventCode.TRANSACTION_ADD_USER_RECEIPT_RETRY_EVENT, transactionRetriedData);
    }
}
