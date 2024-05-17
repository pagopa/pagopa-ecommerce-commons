package it.pagopa.ecommerce.commons.domain.v2.pojos;

import it.pagopa.ecommerce.commons.documents.v2.authorization.TransactionGatewayAuthorizationData;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import javax.annotation.Nullable;
import java.util.Optional;

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

    BaseTransactionWithRequestedAuthorization transactionAtPreviousState;
    @Nullable
    TransactionGatewayAuthorizationData refundRequestedAuthorizationGatewayData;

    /**
     * Primary constructor
     *
     * @param baseTransaction                         base transaction
     * @param refundRequestedAuthorizationGatewayData refund requested optional
     *                                                authorization gateway data
     */
    protected BaseTransactionWithRefundRequested(
            BaseTransactionWithRequestedAuthorization baseTransaction,
            @Nullable TransactionGatewayAuthorizationData refundRequestedAuthorizationGatewayData
    ) {
        super(
                baseTransaction,
                baseTransaction.getTransactionAuthorizationRequestData()
        );
        this.transactionAtPreviousState = baseTransaction;
        this.refundRequestedAuthorizationGatewayData = refundRequestedAuthorizationGatewayData;
    }

    /**
     * Return the transaction gateway authorization data associated to this
     * transaction. If the authorization phase have been completed (so the
     * transaction is a {@link BaseTransactionWithCompletedAuthorization}) the
     * authorization completed gateway data is returned, otherwise the authorization
     * gateway data is returned by the refund requested event, if present
     *
     * @return the optional transaction gateway authorization data
     */
    public Optional<TransactionGatewayAuthorizationData> getTransactionAuthorizationGatewayData() {
        Optional<TransactionGatewayAuthorizationData> optionalGatewayData;
        if (this.transactionAtPreviousState instanceof BaseTransactionWithCompletedAuthorization trx) {
            optionalGatewayData = Optional
                    .of(trx.getTransactionAuthorizationCompletedData().getTransactionGatewayAuthorizationData());
        } else {
            optionalGatewayData = Optional.ofNullable(refundRequestedAuthorizationGatewayData);
        }
        return optionalGatewayData;
    }
}
