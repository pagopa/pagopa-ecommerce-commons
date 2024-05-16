package it.pagopa.ecommerce.commons.documents.v2;

import it.pagopa.ecommerce.commons.documents.v2.authorization.TransactionGatewayAuthorizationData;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Generated;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.annotation.Nullable;

/**
 * Data class that contains information about a transaction for which a refund
 * operation have been requested
 *
 * @see BaseTransactionRefundedData
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Document
@NoArgsConstructor
@Generated
public final class TransactionRefundRequestedData extends BaseTransactionRefundedData {

    /**
     * Gateway authorization data, as retrieved during refund operation
     */
    @Nullable
    private TransactionGatewayAuthorizationData gatewayAuthData;

    /**
     * Constructor
     *
     * @param gatewayAuthData      transaction specific gateway authorization data
     * @param statusBeforeRefunded the transaction status before the refund
     *                             operation
     */
    public TransactionRefundRequestedData(
            @Nullable TransactionGatewayAuthorizationData gatewayAuthData,
            TransactionStatusDto statusBeforeRefunded
    ) {
        super(statusBeforeRefunded);
        this.gatewayAuthData = gatewayAuthData;
    }
}
