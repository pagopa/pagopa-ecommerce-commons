package it.pagopa.ecommerce.commons.documents;

import it.pagopa.ecommerce.commons.domain.TransactionEventCode;
import lombok.Generated;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

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
     * @param transactionId transaction unique id
     * @param rptId         RPT id associated to the transaction
     * @param paymentToken  payment token associated to the transaction
     * @param creationDate  event creation date
     * @param data          event-specific data
     */
    public TransactionActivatedEvent(
            String transactionId,
            String rptId,
            String paymentToken,
            String creationDate,
            TransactionActivatedData data
    ) {
        super(transactionId, rptId, paymentToken, TransactionEventCode.TRANSACTION_ACTIVATED_EVENT, creationDate, data);
    }

    /**
     * Convenience constructor, sets creation date to now.
     *
     * @param transactionId transaction unique id
     * @param rptId         RPT id associated to the transaction
     * @param paymentToken  payment token associated to the transaction
     * @param data          event-specific data
     */
    public TransactionActivatedEvent(
            String transactionId,
            String rptId,
            String paymentToken,
            TransactionActivatedData data
    ) {
        super(transactionId, rptId, paymentToken, TransactionEventCode.TRANSACTION_ACTIVATED_EVENT, data);
    }
}
