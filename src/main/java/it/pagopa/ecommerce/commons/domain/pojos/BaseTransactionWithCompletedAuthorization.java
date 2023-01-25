package it.pagopa.ecommerce.commons.domain.pojos;

import it.pagopa.ecommerce.commons.documents.TransactionAuthorizationStatusUpdateData;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * <p>
 * Marker POJO for a transaction with a completed authorization, either
 * successfully or not.
 * </p>
 * <p>
 * This POJO is needed to abstract inheritance and operations over both
 * {@link BaseTransactionAuthorized} and
 * {@link BaseTransactionWithFailedAuthorization}, and should not be extended by
 * an aggregate class.
 * </p>
 * <p>
 * Generic authorization data is exposed through
 * {@link it.pagopa.ecommerce.commons.documents.TransactionAuthorizationStatusUpdateData
 * TransactionAuthorizationStatusUpdateData}.
 * </p>
 *
 * @see BaseTransaction
 * @see it.pagopa.ecommerce.commons.documents.TransactionAuthorizationStatusUpdateData
 */
@ToString
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public abstract class BaseTransactionWithCompletedAuthorization extends BaseTransactionWithRequestedAuthorization {

    TransactionAuthorizationStatusUpdateData transactionAuthorizationStatusUpdateData;

    /**
     * Primary constructor
     *
     * @param baseTransaction                          base transaction
     * @param transactionAuthorizationStatusUpdateData data related to authorization
     */
    protected BaseTransactionWithCompletedAuthorization(
            BaseTransactionWithRequestedAuthorization baseTransaction,
            TransactionAuthorizationStatusUpdateData transactionAuthorizationStatusUpdateData
    ) {
        super(baseTransaction, baseTransaction.getTransactionAuthorizationRequestData());
        this.transactionAuthorizationStatusUpdateData = transactionAuthorizationStatusUpdateData;
    }
}
