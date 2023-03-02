package it.pagopa.ecommerce.commons.domain.v1.pojos;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * <p>
 * POJO for a transaction with refund requested
 * </p>
 *
 * @see BaseTransactionWithRequestedAuthorization
 */
@ToString
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public abstract class BaseTransactionWithRefundRequested extends BaseTransactionWithRequestedAuthorization {

    /**
     * Primary constructor
     *
     * @param baseTransaction base transaction
     */
    protected BaseTransactionWithRefundRequested(
            BaseTransactionWithRequestedAuthorization baseTransaction
    ) {
        super(
                baseTransaction,
                baseTransaction.getTransactionAuthorizationRequestData()
        );
    }
}
