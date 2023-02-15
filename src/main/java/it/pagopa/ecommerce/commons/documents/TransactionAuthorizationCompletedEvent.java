package it.pagopa.ecommerce.commons.documents;

import it.pagopa.ecommerce.commons.domain.TransactionEventCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Business event corresponding to the reception of a positive authorization
 * response by a PSP (Payment Service Provider), (i.e. the payment has been
 * authorized successfully)
 */
@Document(collection = "eventstore")
@NoArgsConstructor
@ToString(callSuper = true)
public final class TransactionAuthorizationCompletedEvent
        extends
        TransactionEvent<TransactionAuthorizedData> {

    /**
     * Convenience constructor which sets the creation date to now
     *
     * @param transactionId transaction unique id
     * @param data          event-specific data
     */
    public TransactionAuthorizationCompletedEvent(
            String transactionId,
            TransactionAuthorizedData data
    ) {
        super(
                transactionId,
                TransactionEventCode.TRANSACTION_AUTHORIZATION_COMPLETED_EVENT,
                data
        );
    }
}
