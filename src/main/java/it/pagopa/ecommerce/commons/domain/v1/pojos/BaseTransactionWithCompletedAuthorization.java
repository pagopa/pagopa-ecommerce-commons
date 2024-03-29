package it.pagopa.ecommerce.commons.domain.v1.pojos;

import it.pagopa.ecommerce.commons.documents.v1.TransactionAuthorizationCompletedData;
import it.pagopa.ecommerce.commons.generated.server.model.AuthorizationResultDto;
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
 * Generic authorization data is exposed through
 * {@link TransactionAuthorizationCompletedData
 * TransactionAuthorizationStatusUpdateData}.
 * </p>
 *
 * @see BaseTransaction
 * @see TransactionAuthorizationCompletedData
 */
@ToString
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public abstract class BaseTransactionWithCompletedAuthorization extends BaseTransactionWithRequestedAuthorization {

    TransactionAuthorizationCompletedData transactionAuthorizationCompletedData;

    /**
     * Primary constructor
     *
     * @param baseTransaction                       base transaction
     * @param transactionAuthorizationCompletedData data related to authorization
     */
    protected BaseTransactionWithCompletedAuthorization(
            BaseTransactionWithRequestedAuthorization baseTransaction,
            TransactionAuthorizationCompletedData transactionAuthorizationCompletedData
    ) {
        super(baseTransaction, baseTransaction.getTransactionAuthorizationRequestData());
        this.transactionAuthorizationCompletedData = transactionAuthorizationCompletedData;
    }

    /**
     * Check if the transaction was authorized checking if the returned
     * authorization outcome equals to {@link AuthorizationResultDto#OK}
     *
     * @return true iff the transaction was authorized
     */
    public boolean wasTransactionAuthorized() {
        return this.getTransactionAuthorizationCompletedData().getAuthorizationResultDto()
                .equals(AuthorizationResultDto.OK);
    }
}
