package it.pagopa.ecommerce.commons.documents;

import it.pagopa.ecommerce.commons.domain.TransactionEventCode;
import lombok.Generated;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * Event for the request of an activation. Note that this event can happen only
 * in a legacy flow.
 *
 * @see it.pagopa.ecommerce.commons.domain.Transaction Transaction
 */
@Document(collection = "eventstore")
@Generated
@NoArgsConstructor
@ToString(callSuper = true)
public final class TransactionActivationRequestedEvent extends TransactionEvent<TransactionActivationRequestedData> {

    /**
     * Primary constructor
     *
     * @param transactionId transaction unique id
     * @param creationDate  event creation date
     * @param data          event-specific data
     */
    @PersistenceConstructor
    public TransactionActivationRequestedEvent(
            String transactionId,
            String creationDate,
            TransactionActivationRequestedData data
    ) {
        super(
                transactionId,
                TransactionEventCode.TRANSACTION_ACTIVATION_REQUESTED_EVENT,
                creationDate,
                data
        );
    }

    /**
     * Convenience constructor which sets the creation date to now
     *
     * @param transactionId transaction unique id
     * @param data          event-specific data
     */
    @PersistenceConstructor
    public TransactionActivationRequestedEvent(
            String transactionId,
            TransactionActivationRequestedData data
    ) {
        super(transactionId, TransactionEventCode.TRANSACTION_ACTIVATION_REQUESTED_EVENT, data);
    }
}
