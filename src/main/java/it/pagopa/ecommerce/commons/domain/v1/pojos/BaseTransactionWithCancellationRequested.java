package it.pagopa.ecommerce.commons.domain.v1.pojos;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * <p>
 * POJO for a transaction with a cancellation requested by the user
 * </p>
 *
 * @see BaseTransactionWithPaymentToken
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
