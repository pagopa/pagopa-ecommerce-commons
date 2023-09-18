package it.pagopa.ecommerce.commons.documents.v2;

import it.pagopa.ecommerce.commons.domain.v2.TransactionEventCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Shared transaction closure correlated events structure
 *
 * @see TransactionClosedEvent
 * @see TransactionClosureErrorEvent
 * @see TransactionClosureFailedEvent
 */
@Document(collection = "eventstore")
@NoArgsConstructor
@ToString(callSuper = true)
public abstract sealed class BaseTransactionClosureEvent extends
        TransactionEvent<TransactionClosureData>permits TransactionClosedEvent,TransactionClosureFailedEvent {

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
