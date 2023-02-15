package it.pagopa.ecommerce.commons.domain.pojos;

import it.pagopa.ecommerce.commons.documents.TransactionClosedEvent;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * <p>
 * POJO for a notified transaction. Note that this POJO is used both for
 * successful and unsuccessful notification
 * </p>
 *
 * @see BaseTransaction
 * @see TransactionClosedEvent TransactionClosureSentEvent
 */
@ToString
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public abstract class BaseTransactionWithUserReceipt extends BaseTransactionClosed {

    /**
     * Main constructor.
     *
     * @param baseTransaction           transaction to extend with authorization
     *                                  data
     * @param transactionAddReceiptData authorization data
     */
    protected BaseTransactionWithUserReceipt(
            BaseTransactionClosed baseTransaction
    ) {
        super(baseTransaction);
    }
}
