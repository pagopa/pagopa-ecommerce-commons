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
     * Gateway authorization data, as retrieved during refund operation. It can be
     * null if no authorization have been completed for the current transaction (ex
     * refund started for a transaction in
     * {@link it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto#AUTHORIZATION_REQUESTED}
     * status and no gateway api exists to retrieve authorization outcome
     * asynchronously (such as NPG GET orders))
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
