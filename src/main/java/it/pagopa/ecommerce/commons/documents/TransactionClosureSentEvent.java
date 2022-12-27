package it.pagopa.ecommerce.commons.documents;

import it.pagopa.ecommerce.commons.domain.TransactionEventCode;
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
public final class TransactionClosureSentEvent extends TransactionEvent<TransactionClosureSendData> {

    /**
     * Convenience constructor which sets the creation date to now
     *
     * @param transactionId transaction unique id
     * @param data          event-specific data
     */
    public TransactionClosureSentEvent(
            String transactionId,
            TransactionClosureSendData data
    ) {
        super(transactionId, TransactionEventCode.TRANSACTION_CLOSURE_SENT_EVENT, data);
    }
}
