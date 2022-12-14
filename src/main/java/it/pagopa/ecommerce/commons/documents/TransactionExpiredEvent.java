package it.pagopa.ecommerce.commons.documents;

import it.pagopa.ecommerce.commons.domain.TransactionEventCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

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
     * @param noticeCodes   notice code list
     * @param data          event-specific data
     */
    public TransactionExpiredEvent(
            String transactionId,
            List<NoticeCode> noticeCodes,
            TransactionExpiredData data
    ) {
        super(transactionId, noticeCodes, TransactionEventCode.TRANSACTION_EXPIRED_EVENT, data);
    }
}
