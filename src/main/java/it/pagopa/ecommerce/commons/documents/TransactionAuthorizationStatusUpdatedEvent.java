package it.pagopa.ecommerce.commons.documents;

import it.pagopa.ecommerce.commons.domain.TransactionEventCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * Business event corresponding to the reception of an authorization response by
 * a PSP (Payment Service Provider), either positive (i.e. the payment has been
 * authorized successfully) or negative (i.e. the payment authorization has been
 * rejected).
 */
@Document(collection = "eventstore")
@NoArgsConstructor
@ToString(callSuper = true)
public final class TransactionAuthorizationStatusUpdatedEvent
        extends
        TransactionEvent<TransactionAuthorizationStatusUpdateData> {

    /**
     * Convenience constructor which sets the creation date to now
     *
     * @param transactionId transaction unique id
     * @param data          event-specific data
     */
    public TransactionAuthorizationStatusUpdatedEvent(
            String transactionId,
            TransactionAuthorizationStatusUpdateData data
    ) {
        super(
                transactionId,
                TransactionEventCode.TRANSACTION_AUTHORIZATION_STATUS_UPDATED_EVENT,
                data
        );
    }
}
