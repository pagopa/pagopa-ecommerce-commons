package it.pagopa.ecommerce.commons.documents.v1;

import it.pagopa.ecommerce.commons.domain.v1.TransactionEventCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Business event corresponding to a transaction closure KO being sent. Closing a
 * transaction notifies Nodo that the transaction has been finalized.
 */
@Document(collection = "eventstore")
@NoArgsConstructor
@ToString(callSuper = true)
public final class TransactionClosureFailedEvent extends BaseTransactionClosureEvent {

    /**
     * Convenience constructor which sets the creation date to now
     *
     * @param transactionId          transaction unique id
     * @param transactionClosureData the transaction closure operation data
     */
    public TransactionClosureFailedEvent(
            String transactionId,
            TransactionClosureData transactionClosureData
    ) {
        super(transactionId, TransactionEventCode.TRANSACTION_CLOSURE_FAILED_EVENT, transactionClosureData);
    }
}
