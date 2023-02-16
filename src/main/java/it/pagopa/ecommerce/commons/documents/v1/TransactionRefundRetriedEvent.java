package it.pagopa.ecommerce.commons.documents.v1;

import it.pagopa.ecommerce.commons.domain.v1.TransactionEventCode;
import lombok.Generated;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Business event corresponding to a retry of transaction closure. This event
 * maps to a transient state for transactions.
 */
@Document(collection = "eventstore")
@Generated
@NoArgsConstructor
@ToString(callSuper = true)
public final class TransactionRefundRetriedEvent extends TransactionEvent<TransactionRetriedData> {

    /**
     * Convenience constructor which sets the creation date to now
     *
     * @param transactionId          transaction unique id
     * @param transactionRetriedData retry count data
     */
    public TransactionRefundRetriedEvent(
            String transactionId,
            TransactionRetriedData transactionRetriedData
    ) {
        super(transactionId, TransactionEventCode.TRANSACTION_REFUND_RETRIED_EVENT, transactionRetriedData);
    }
}
