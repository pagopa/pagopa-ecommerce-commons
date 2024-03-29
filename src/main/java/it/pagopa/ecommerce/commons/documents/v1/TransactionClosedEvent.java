package it.pagopa.ecommerce.commons.documents.v1;

import it.pagopa.ecommerce.commons.domain.v1.TransactionEventCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Business event corresponding to a transaction closure OK being sent. This
 * action notifies Nodo that the transaction has been finalized.
 */
@Document(collection = "eventstore")
@NoArgsConstructor
@ToString(callSuper = true)
public final class TransactionClosedEvent extends BaseTransactionClosureEvent {

    /**
     * Convenience constructor which sets the creation date to now
     *
     * @param transactionId          transaction unique id
     * @param transactionClosureData the transaction closure operation data
     */
    public TransactionClosedEvent(
            String transactionId,
            TransactionClosureData transactionClosureData
    ) {
        super(transactionId, TransactionEventCode.TRANSACTION_CLOSED_EVENT, transactionClosureData);
    }
}
