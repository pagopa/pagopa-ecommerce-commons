package it.pagopa.ecommerce.commons.documents;

import it.pagopa.ecommerce.commons.domain.TransactionEventCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Business event corresponding to the reception of a negative authorization
 * response by a PSP (Payment Service Provider) (i.e. the payment authorization
 * has been rejected).
 */
@Document(collection = "eventstore")
@NoArgsConstructor
@ToString(callSuper = true)
public final class TransactionAuthorizationFailedEvent
        extends
        TransactionEvent<Void> {

    /**
     * Convenience constructor which sets the creation date to now
     *
     * @param transactionId transaction unique id
     */
    public TransactionAuthorizationFailedEvent(
            String transactionId
    ) {
        super(
                transactionId,
                TransactionEventCode.TRANSACTION_AUTHORIZED,
                null
        );
    }
}
