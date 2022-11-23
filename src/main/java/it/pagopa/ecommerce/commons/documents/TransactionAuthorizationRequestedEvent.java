package it.pagopa.ecommerce.commons.documents;

import it.pagopa.ecommerce.commons.domain.TransactionEventCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "eventstore")
@NoArgsConstructor
@ToString(callSuper = true)
public final class TransactionAuthorizationRequestedEvent
        extends
        TransactionEvent<TransactionAuthorizationRequestData> {

    public TransactionAuthorizationRequestedEvent(
            String transactionId,
            String rptId,
            String paymentToken,
            TransactionAuthorizationRequestData data
    ) {
        super(transactionId, rptId, paymentToken, TransactionEventCode.TRANSACTION_AUTHORIZATION_REQUESTED_EVENT, data);
    }
}
