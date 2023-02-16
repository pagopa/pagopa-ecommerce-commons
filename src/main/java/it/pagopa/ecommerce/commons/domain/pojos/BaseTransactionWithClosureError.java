package it.pagopa.ecommerce.commons.domain.pojos;

import it.pagopa.ecommerce.commons.documents.v1.TransactionClosureErrorEvent;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * <p>
 * POJO for a transaction that incurred into an error while trying to close.
 * Currently it does not expose any additional fields, as the associated event
 * does hold any additional data.
 * </p>
 *
 * @see BaseTransaction
 * @see TransactionClosureErrorEvent
 */
@ToString
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public abstract class BaseTransactionWithClosureError extends BaseTransactionWithCompletedAuthorization {

    TransactionClosureErrorEvent event;

    /**
     * Primary constructor
     *
     * @param baseTransaction base transaction
     * @param event           data related to closure error event
     */
    protected BaseTransactionWithClosureError(
            BaseTransactionWithCompletedAuthorization baseTransaction,
            TransactionClosureErrorEvent event
    ) {
        super(baseTransaction, baseTransaction.getTransactionAuthorizationCompletedData());
        this.event = event;
    }
}
