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
     * @param rptId         RPT id associated to the transaction
     * @param paymentToken  payment token related to this transaction
     * @param data          event-specific data
     */
    public TransactionExpiredEvent(
            String transactionId,
            String rptId,
            String paymentToken,
            TransactionExpiredData data
    ) {
        super(transactionId, rptId, paymentToken, TransactionEventCode.TRANSACTION_EXPIRED_EVENT, data);
    }
}
