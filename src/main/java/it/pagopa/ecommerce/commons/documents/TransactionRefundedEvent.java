package it.pagopa.ecommerce.commons.documents;

import it.pagopa.ecommerce.commons.domain.TransactionEventCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Business event corresponding to a transaction refund.
 */
@Document(collection = "eventstore")
@NoArgsConstructor
@ToString(callSuper = true)
public final class TransactionRefundedEvent extends TransactionEvent<TransactionRefundedData> {
    /**
     * Convenience constructor which sets the creation date to now
     *
     * @param transactionId transaction unique id
     * @param data          event-specific data
     */
    public TransactionRefundedEvent(
            String transactionId,
            TransactionRefundedData data
    ) {
        super(transactionId, TransactionEventCode.TRANSACTION_REFUNDED_EVENT, data);
    }
}
