package it.pagopa.ecommerce.commons.documents.v2;

import it.pagopa.ecommerce.commons.domain.v2.TransactionEventCode;
import lombok.Generated;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Transaction closure synthetic event: this event is raised by eCommerce when a
 * send payment result is received by Nodo for a transaction not in CLOSED
 * state. This event allow for transaction processing recover from
 * CLOSURE_REQUESTED/CLOSURE_ERROR statuses in case Nodo have processed close
 * payment correctly
 */
@Document(collection = "eventstore")
@Generated
@NoArgsConstructor
@ToString(callSuper = true)
public final class TransactionClosureSyntheticEvent extends TransactionEvent<TransactionClosureData> {

    /**
     * Primary constructor
     *
     * @param transactionId transaction unique id
     */
    public TransactionClosureSyntheticEvent(
            String transactionId
    ) {
        // transaction closure synthetic event assumes that close payment response
        // outcome is always OK (Nodo did not send SendPaymentResult requests for KO
        // outcomes)
        super(
                transactionId,
                TransactionEventCode.TRANSACTION_CLOSURE_SYNTHETIC_EVENT,
                new TransactionClosureData(TransactionClosureData.Outcome.OK, false)
        );
    }

}
