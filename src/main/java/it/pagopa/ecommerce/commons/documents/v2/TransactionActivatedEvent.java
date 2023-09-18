package it.pagopa.ecommerce.commons.documents.v2;

import it.pagopa.ecommerce.commons.domain.v2.TransactionEventCode;
import lombok.Generated;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Activation event. Semantically this event blocks modification on payment data
 * by public entities for some time and allows citizen to authorize a payment.
 */
@Document(collection = "eventstore")
@Generated
@NoArgsConstructor
@ToString(callSuper = true)
public final class TransactionActivatedEvent extends TransactionEvent<TransactionActivatedData> {

    /**
     * Primary constructor
     *
     * @param transactionId transaction unique id
     * @param creationDate  event creation date
     * @param data          event-specific data
     */
    public TransactionActivatedEvent(
            String transactionId,
            String creationDate,
            TransactionActivatedData data
    ) {
        super(transactionId, TransactionEventCode.TRANSACTION_ACTIVATED_EVENT, creationDate, data);
    }

    /**
     * Convenience constructor, sets creation date to now.
     *
     * @param transactionId transaction unique id
     * @param data          event-specific data
     */
    public TransactionActivatedEvent(
            String transactionId,
            TransactionActivatedData data
    ) {
        super(transactionId, TransactionEventCode.TRANSACTION_ACTIVATED_EVENT, data);
    }
}
