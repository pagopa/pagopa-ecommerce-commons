package it.pagopa.ecommerce.commons.documents.v2;

import it.pagopa.ecommerce.commons.domain.v2.TransactionEventCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Business event corresponding to a transaction closure. This event maps to a
 * transient state for transactions.
 */
@Document(collection = "eventstore")
@NoArgsConstructor
@ToString(callSuper = true)
public final class TransactionClosureRequestedEvent extends TransactionEvent<Void> {

    /**
     * Convenience constructor which sets the creation date to now
     *
     * @param transactionId transaction unique id
     */
    public TransactionClosureRequestedEvent(
            String transactionId
    ) {
        super(transactionId, TransactionEventCode.TRANSACTION_CLOSURE_REQUESTED_EVENT, null);
    }
}
