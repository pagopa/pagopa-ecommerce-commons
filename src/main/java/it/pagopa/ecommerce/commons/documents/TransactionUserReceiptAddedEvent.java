package it.pagopa.ecommerce.commons.documents;

import it.pagopa.ecommerce.commons.domain.TransactionEventCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "eventstore")
@NoArgsConstructor
@ToString(callSuper = true)
public final class TransactionUserReceiptAddedEvent extends TransactionEvent<TransactionAddReceiptData> {

    public TransactionUserReceiptAddedEvent(
            String transactionId,
            String rptId,
            String paymentToken,
            TransactionAddReceiptData data
    ) {
        super(transactionId, rptId, paymentToken, TransactionEventCode.TRANSACTION_USER_RECEIPT_ADDED_EVENT, data);
    }
}
