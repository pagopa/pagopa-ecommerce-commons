package it.pagopa.ecommerce.commons.documents.v2;

import it.pagopa.ecommerce.commons.domain.v2.TransactionEventCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Business event corresponding to a transaction refund requested for a given
 * transaction.
 */
@Document(collection = "eventstore")
@NoArgsConstructor
@ToString(callSuper = true)
public final class TransactionRefundRequestedEvent extends TransactionEvent<TransactionRefundRequestedData> {
    /**
     * Convenience constructor which sets the creation date to now
     *
     * @param transactionId transaction unique id
     * @param data          event-specific data
     */
    public TransactionRefundRequestedEvent(
            String transactionId,
            TransactionRefundRequestedData data
    ) {
        super(transactionId, TransactionEventCode.TRANSACTION_REFUND_REQUESTED_EVENT, data);
    }
}
