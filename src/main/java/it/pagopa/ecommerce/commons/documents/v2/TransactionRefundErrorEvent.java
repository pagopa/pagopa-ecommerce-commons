package it.pagopa.ecommerce.commons.documents.v2;

import it.pagopa.ecommerce.commons.domain.v2.TransactionEventCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Business event corresponding to an error during transaction refund processing
 */
@Document(collection = "eventstore")
@NoArgsConstructor
@ToString(callSuper = true)
public final class TransactionRefundErrorEvent extends TransactionEvent<TransactionRefundErrorData> {
    /**
     * Convenience constructor which sets the creation date to now
     *
     * @param transactionId transaction unique id
     * @param data          event-specific data
     */
    public TransactionRefundErrorEvent(
            String transactionId,
            TransactionRefundErrorData data
    ) {
        super(transactionId, TransactionEventCode.TRANSACTION_REFUND_ERROR_EVENT, data);
    }
}
