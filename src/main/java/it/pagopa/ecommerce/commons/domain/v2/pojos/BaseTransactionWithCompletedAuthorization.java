package it.pagopa.ecommerce.commons.domain.v2.pojos;

import it.pagopa.ecommerce.commons.documents.v2.authorization.NpgTransactionGatewayAuthorizationData;
import it.pagopa.ecommerce.commons.documents.v2.authorization.PgsTransactionGatewayAuthorizationData;
import it.pagopa.ecommerce.commons.documents.v2.authorization.RedirectTransactionGatewayAuthorizationData;
import it.pagopa.ecommerce.commons.documents.v2.authorization.TransactionGatewayAuthorizationData;
import it.pagopa.ecommerce.commons.generated.npg.v1.dto.OperationResultDto;
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
 * {@link it.pagopa.ecommerce.commons.documents.v2.TransactionAuthorizationCompletedData
 * TransactionAuthorizationStatusUpdateData}.
 * </p>
 *
 * @see BaseTransaction
 * @see it.pagopa.ecommerce.commons.documents.v2.TransactionAuthorizationCompletedData
 */
@ToString
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public abstract class BaseTransactionWithCompletedAuthorization extends BaseTransactionWithRequestedAuthorization {

    it.pagopa.ecommerce.commons.documents.v2.TransactionAuthorizationCompletedData transactionAuthorizationCompletedData;

    /**
     * Primary constructor
     *
     * @param baseTransaction                       base transaction
     * @param transactionAuthorizationCompletedData data related to authorization
     */
    protected BaseTransactionWithCompletedAuthorization(
            BaseTransactionWithRequestedAuthorization baseTransaction,
            it.pagopa.ecommerce.commons.documents.v2.TransactionAuthorizationCompletedData transactionAuthorizationCompletedData
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
        TransactionGatewayAuthorizationData data = this.getTransactionAuthorizationCompletedData()
                .getTransactionGatewayAuthorizationData();
        if (data instanceof PgsTransactionGatewayAuthorizationData p) {
            return p.getAuthorizationResultDto().equals(AuthorizationResultDto.OK);
        } else if (data instanceof NpgTransactionGatewayAuthorizationData n) {
            return n.getOperationResult().equals(OperationResultDto.EXECUTED);
        } else if (data instanceof RedirectTransactionGatewayAuthorizationData r) {
            return r.getOutcome().equals(RedirectTransactionGatewayAuthorizationData.Outcome.OK);
        }
        return false;
    }
}
