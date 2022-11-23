package it.pagopa.ecommerce.commons.documents;

import it.pagopa.ecommerce.commons.domain.TransactionEventCode;
import lombok.Generated;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "eventstore")
@Generated
@NoArgsConstructor
@ToString(callSuper = true)
public final class TransactionActivationRequestedEvent extends TransactionEvent<TransactionActivationRequestedData> {

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

    @PersistenceConstructor
    public TransactionActivationRequestedEvent(
            String transactionId,
            String rptId,
            TransactionActivationRequestedData data
    ) {
        super(transactionId, rptId, null, TransactionEventCode.TRANSACTION_ACTIVATION_REQUESTED_EVENT, data);
    }
}
