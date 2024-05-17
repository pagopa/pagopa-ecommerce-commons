package it.pagopa.ecommerce.commons.documents.v2;

import it.pagopa.ecommerce.commons.documents.v2.authorization.TransactionGatewayAuthorizationData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Generated;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.annotation.Nullable;

/**
 * Data related to retry event for a transaction refund operation
 *
 * @see BaseTransactionRetriedData
 */

@Data
@EqualsAndHashCode(callSuper = true)
@Document
@NoArgsConstructor
@Generated
public final class TransactionRefundRetriedData extends BaseTransactionRetriedData {

    @Nullable
    private TransactionGatewayAuthorizationData transactionGatewayAuthorizationData;

    /**
     * Constructor
     *
     * @param transactionGatewayAuthorizationData the transaction gateway
     *                                            authorization data as retrieved
     *                                            during refund operation
     * @param retryCount                          the retry event counter
     */
    public TransactionRefundRetriedData(
            @Nullable TransactionGatewayAuthorizationData transactionGatewayAuthorizationData,
            Integer retryCount
    ) {
        super(retryCount);
        this.transactionGatewayAuthorizationData = transactionGatewayAuthorizationData;
    }
}
