package it.pagopa.ecommerce.commons.documents.v1;

import it.pagopa.ecommerce.commons.domain.v1.TransactionEventCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Business event corresponding to a transaction closure being sent. This action
 * notifies Nodo that the transaction has been finalized.
 */
@Document(collection = "eventstore")
@NoArgsConstructor
@ToString(callSuper = true)
public sealed class BaseTransactionClosureEvent extends
        TransactionEvent<TransactionClosureData>permits TransactionClosedEvent,TransactionClosureErrorEvent,TransactionClosureFailedEvent {

    /**
     * Convenience constructor which sets the creation date to now
     *
     * @param transactionId          transaction unique id
     * @param transactionEventCode   the transaction event code
     * @param transactionClosureData the transaction closure operation data
     */
    BaseTransactionClosureEvent(
            String transactionId,
            TransactionEventCode transactionEventCode,
            TransactionClosureData transactionClosureData
    ) {
        super(transactionId, transactionEventCode, transactionClosureData);
    }
}
