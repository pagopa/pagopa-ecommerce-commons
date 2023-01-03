package it.pagopa.ecommerce.commons.documents;

import it.pagopa.ecommerce.commons.domain.TransactionEventCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Business event corresponding to the user requesting a payment authorization
 * from a PSP (Payments Service Provider).
 */
@Document(collection = "eventstore")
@NoArgsConstructor
@ToString(callSuper = true)
public final class TransactionAuthorizationRequestedEvent
        extends
        TransactionEvent<TransactionAuthorizationRequestData> {

    /**
     * Convenience constructor which sets the creation date to now
     *
     * @param transactionId transaction unique id
     * @param data          event-specific data
     */
    public TransactionAuthorizationRequestedEvent(
            String transactionId,
            TransactionAuthorizationRequestData data
    ) {
        super(transactionId, TransactionEventCode.TRANSACTION_AUTHORIZATION_REQUESTED_EVENT, data);
    }
}
