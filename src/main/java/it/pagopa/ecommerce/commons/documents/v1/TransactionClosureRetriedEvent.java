package it.pagopa.ecommerce.commons.documents.v1;

import it.pagopa.ecommerce.commons.domain.TransactionEventCode;
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
public final class TransactionClosureRetriedEvent extends TransactionEvent<TransactionRetriedData> {

    /**
     * Convenience constructor which sets the creation date to now
     *
     * @param transactionId          transaction unique id
     * @param transactionRetriedData retry count data
     */
    public TransactionClosureRetriedEvent(
            String transactionId,
            TransactionRetriedData transactionRetriedData
    ) {
        super(transactionId, TransactionEventCode.TRANSACTION_CLOSURE_RETRIED_EVENT, transactionRetriedData);
    }
}
