package it.pagopa.ecommerce.commons.domain.pojos;

import it.pagopa.ecommerce.commons.documents.TransactionClosedEvent;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * <p>
 * POJO for a closed transaction. Note that this POJO is used both for
 * successful and unsuccessful closures (for closures that incurred into errors,
 * see
 * {@link it.pagopa.ecommerce.commons.domain.pojos.BaseTransactionWithClosureError}.
 * </p>
 *
 * @see BaseTransaction
 * @see TransactionClosedEvent TransactionClosureSentEvent
 */
@ToString
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public abstract class BaseTransactionClosed extends BaseTransactionWithCompletedAuthorization {

    /**
     * Primary constructor
     *
     * @param baseTransaction            base transaction
     * @param transactionClosureSendData data related to closure sending event
     */
    protected BaseTransactionClosed(
            BaseTransactionWithCompletedAuthorization baseTransaction
    ) {
        super(baseTransaction, baseTransaction.getTransactionAuthorizationStatusUpdateData());
    }
}
