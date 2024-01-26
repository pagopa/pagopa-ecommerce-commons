package it.pagopa.ecommerce.commons.domain.v2.pojos;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * <p>
 * POJO for a closure requested event
 * </p>
 *
 * @see BaseTransactionWithCompletedAuthorization
 */
@ToString
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public abstract class BaseTransactionWithClosureRequested extends BaseTransactionWithCompletedAuthorization {

    /**
     * Primary constructor
     *
     * @param baseTransaction base transaction
     */
    protected BaseTransactionWithClosureRequested(
            BaseTransactionWithCompletedAuthorization baseTransaction
    ) {
        super(
                baseTransaction,
                baseTransaction.getTransactionAuthorizationCompletedData()
        );
    }

}
