package it.pagopa.ecommerce.commons.documents;

import it.pagopa.ecommerce.commons.domain.TransactionEventCode;
import lombok.Generated;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * Activation event. Semantically this event blocks modification on payment data
 * by public entities for some time and allows citizen to authorize a payment.
 */
@Document(collection = "eventstore")
@Generated
@NoArgsConstructor
@ToString(callSuper = true)
public final class TransactionActivatedEvent extends TransactionEvent<TransactionActivatedData> {

    /**
     * Primary constructor
     *
     * @param transactionId  transaction unique id
     * @param noticeCodeList notice code list
     * @param creationDate   event creation date
     * @param data           event-specific data
     */
    public TransactionActivatedEvent(
            String transactionId,
            List<NoticeCode> noticeCodeList,
            String creationDate,
            TransactionActivatedData data
    ) {
        super(transactionId, noticeCodeList, TransactionEventCode.TRANSACTION_ACTIVATED_EVENT, creationDate, data);
    }

    /**
     * Convenience constructor, sets creation date to now.
     *
     * @param transactionId  transaction unique id
     * @param noticeCodeList notice code list
     * @param data           event-specific data
     */
    public TransactionActivatedEvent(
            String transactionId,
            List<NoticeCode> noticeCodeList,
            TransactionActivatedData data
    ) {
        super(transactionId, noticeCodeList, TransactionEventCode.TRANSACTION_ACTIVATED_EVENT, data);
    }
}
