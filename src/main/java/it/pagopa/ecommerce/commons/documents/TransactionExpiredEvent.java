package it.pagopa.ecommerce.commons.documents;

import it.pagopa.ecommerce.commons.domain.TransactionEventCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Business event corresponding to a transaction expiration.
 */
@Document(collection = "eventstore")
@NoArgsConstructor
@ToString(callSuper = true)
public final class TransactionExpiredEvent extends TransactionEvent<TransactionExpiredData> {
    /**
     * Convenience constructor which sets the creation date to now
     *
     * @param transactionId transaction unique id
     * @param data          event-specific data
     */
    public TransactionExpiredEvent(
            String transactionId,
            TransactionExpiredData data
    ) {
        super(transactionId, TransactionEventCode.TRANSACTION_EXPIRED_EVENT, data);
    }
}
