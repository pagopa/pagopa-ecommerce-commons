package it.pagopa.ecommerce.commons.documents.v2;

import it.pagopa.ecommerce.commons.domain.v2.TransactionEventCode;
import lombok.Generated;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Business event corresponding to a retry of transaction authorization
 * requested. This event maps to a transient state for transactions.
 */
@Document(collection = "eventstore")
@Generated
@NoArgsConstructor
@ToString(callSuper = true)
public final class TransactionAuthorizationOutcomeWaitingEvent extends TransactionEvent<TransactionRetriedData> {

    /**
     * Convenience constructor for transaction authorization outcome waiting event
     * with eventCode, transactionId and retriedData
     *
     * @param transactionId          transaction unique id
     * @param transactionRetriedData retry count data
     */
    public TransactionAuthorizationOutcomeWaitingEvent(
            String transactionId,
            TransactionRetriedData transactionRetriedData
    ) {
        super(
                transactionId,
                TransactionEventCode.TRANSACTION_AUTHORIZATION_OUTCOME_WAITING_EVENT,
                transactionRetriedData
        );
    }
}
