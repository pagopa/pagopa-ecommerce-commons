package it.pagopa.ecommerce.commons.documents.v2;

import it.pagopa.ecommerce.commons.domain.v2.TransactionEventCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Business event generated when the user cancel the current payment process
 */
@Document(collection = "eventstore")
@NoArgsConstructor
@ToString(callSuper = true)
public final class TransactionUserCanceledEvent extends TransactionEvent<Void> {

    /**
     * Convenience constructor which sets the creation date to now
     *
     * @param transactionId transaction unique id
     */
    public TransactionUserCanceledEvent(
            String transactionId
    ) {
        super(transactionId, TransactionEventCode.TRANSACTION_USER_CANCELED_EVENT, null);
    }
}
