package it.pagopa.ecommerce.commons.documents;

import it.pagopa.ecommerce.commons.domain.TransactionEventCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

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
     * @param noticeCodes   notice code list
     * @param data          event-specific data
     */
    public TransactionRefundedEvent(
            String transactionId,
            List<NoticeCode> noticeCodes,
            TransactionRefundedData data
    ) {
        super(transactionId, noticeCodes, TransactionEventCode.TRANSACTION_REFUNDED_EVENT, data);
    }
}
