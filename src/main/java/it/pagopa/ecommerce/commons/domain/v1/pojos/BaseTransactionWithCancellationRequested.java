package it.pagopa.ecommerce.commons.domain.v1.pojos;

import it.pagopa.ecommerce.commons.documents.v1.TransactionExpiredEvent;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * <p>
 * POJO for an expired transaction.
 * </p>
 *
 * @see BaseTransaction
 * @see TransactionExpiredEvent
 */
@ToString
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public abstract class BaseTransactionWithCancellationRequested extends BaseTransactionWithPaymentToken {

    /**
     * Primary constructor
     *
     * @param baseTransaction base transaction
     */
    protected BaseTransactionWithCancellationRequested(
            BaseTransactionWithPaymentToken baseTransaction
    ) {
        super(
                baseTransaction,
                baseTransaction.getTransactionActivatedData()
        );
    }
}
