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
     * @param rptId         RPT id associated to the transaction
     * @param paymentToken  payment token related to this transaction
     * @param data          event-specific data
     */
    public TransactionRefundedEvent(
            String transactionId,
            String rptId,
            String paymentToken,
            TransactionRefundedData data
    ) {
        super(transactionId, rptId, paymentToken, TransactionEventCode.TRANSACTION_REFUNDED_EVENT, data);
    }
}
