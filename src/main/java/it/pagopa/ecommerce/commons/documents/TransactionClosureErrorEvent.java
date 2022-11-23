package it.pagopa.ecommerce.commons.documents;

import it.pagopa.ecommerce.commons.domain.TransactionEventCode;
import lombok.Generated;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "eventstore")
@Generated
@NoArgsConstructor
@ToString(callSuper = true)
public final class TransactionClosureErrorEvent extends TransactionEvent<Void> {

    public TransactionClosureErrorEvent(
            String transactionId,
            String rptId,
            String paymentToken
    ) {
        super(transactionId, rptId, paymentToken, TransactionEventCode.TRANSACTION_CLOSURE_ERROR_EVENT, null);
    }
}
