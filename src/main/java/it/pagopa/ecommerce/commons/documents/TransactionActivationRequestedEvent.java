package it.pagopa.ecommerce.commons.documents;

import it.pagopa.ecommerce.commons.domain.TransactionEventCode;
import lombok.Generated;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

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
     * @param rptId         RPT id associated to the transaction
     * @param creationDate  event creation date
     * @param data          event-specific data
     */
    @PersistenceConstructor
    public TransactionActivationRequestedEvent(
            String transactionId,
            String rptId,
            String creationDate,
            TransactionActivationRequestedData data
    ) {
        super(
                transactionId,
                rptId,
                null,
                TransactionEventCode.TRANSACTION_ACTIVATION_REQUESTED_EVENT,
                creationDate,
                data
        );
    }

    /**
     * Convenience constructor which sets the creation date to now
     *
     * @param transactionId transaction unique id
     * @param rptId         RPT id associated to the transaction
     * @param data          event-specific data
     */
    @PersistenceConstructor
    public TransactionActivationRequestedEvent(
            String transactionId,
            String rptId,
            TransactionActivationRequestedData data
    ) {
        super(transactionId, rptId, null, TransactionEventCode.TRANSACTION_ACTIVATION_REQUESTED_EVENT, data);
    }
}
