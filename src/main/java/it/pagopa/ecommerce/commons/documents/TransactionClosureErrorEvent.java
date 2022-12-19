package it.pagopa.ecommerce.commons.documents;

import it.pagopa.ecommerce.commons.domain.TransactionEventCode;
import lombok.Generated;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * Business event corresponding to a transaction closure incurring in an error.
 * This event maps to a transient state for transactions.
 */
@Document(collection = "eventstore")
@Generated
@NoArgsConstructor
@ToString(callSuper = true)
public final class TransactionClosureErrorEvent extends TransactionEvent<Void> {

    /**
     * Convenience constructor which sets the creation date to now
     *
     * @param transactionId transaction unique id
     * @param noticeCodes   notice code list
     */
    public TransactionClosureErrorEvent(
            String transactionId,
            List<NoticeCode> noticeCodes
    ) {
        super(transactionId, noticeCodes, TransactionEventCode.TRANSACTION_CLOSURE_ERROR_EVENT, null);
    }
}
