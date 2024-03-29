package it.pagopa.ecommerce.commons.domain.v1.pojos;

import it.pagopa.ecommerce.commons.documents.v1.TransactionClosureData;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * <p>
 * POJO for a closed transaction
 * </p>
 *
 * @see BaseTransactionWithCompletedAuthorization
 * @see TransactionClosureData
 */
@ToString
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public abstract class BaseTransactionClosed extends BaseTransactionWithCompletedAuthorization {
    TransactionClosureData transactionClosureData;

    /**
     * Primary constructor
     *
     * @param baseTransaction        base transaction
     * @param transactionClosureData transaction closure data
     */
    protected BaseTransactionClosed(
            BaseTransactionWithCompletedAuthorization baseTransaction,
            TransactionClosureData transactionClosureData
    ) {
        super(
                baseTransaction,
                baseTransaction.getTransactionAuthorizationCompletedData()
        );

        this.transactionClosureData = transactionClosureData;
    }
}
