package it.pagopa.ecommerce.commons.documents.v1;

import it.pagopa.ecommerce.commons.domain.v1.TransactionEventCode;
import lombok.Generated;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Business event corresponding to a transaction closure incurring in an error.
 * This event maps to a transient state for transactions.
 */
@Document(collection = "eventstore")
@Generated
@NoArgsConstructor
@ToString(callSuper = true)
public final class TransactionClosureErrorEvent extends BaseTransactionClosureEvent {

    /**
     * Convenience constructor which sets the creation date to now
     *
     * @param transactionId          transaction unique id
     * @param transactionClosureData transaction closure data
     */
    public TransactionClosureErrorEvent(
            String transactionId,
            TransactionClosureData transactionClosureData
    ) {
        super(transactionId, TransactionEventCode.TRANSACTION_CLOSURE_ERROR_EVENT, transactionClosureData);
    }
}
