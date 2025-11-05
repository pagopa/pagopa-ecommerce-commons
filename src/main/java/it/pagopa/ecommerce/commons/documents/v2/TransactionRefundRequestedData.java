package it.pagopa.ecommerce.commons.documents.v2;

import com.fasterxml.jackson.annotation.JsonInclude;
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
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private TransactionGatewayAuthorizationData gatewayAuthData;

    /**
     * Specifies if the refund was initiated manually or triggered automatically. A
     * manual refund typically occurs when an operator explicitly initiates a
     * refund. An automatic refund is requested directly by the system flow in
     * response to specific conditions.
     */
    @Nullable
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private RefundTrigger refundTrigger;

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

    /**
     * Constructor
     *
     * @param gatewayAuthData      transaction specific gateway authorization data
     * @param statusBeforeRefunded the transaction status before the refund
     *                             operation
     * @param refundTrigger        whether the refund was initiated manually or
     *                             triggered automatically
     */
    public TransactionRefundRequestedData(
            @Nullable TransactionGatewayAuthorizationData gatewayAuthData,
            TransactionStatusDto statusBeforeRefunded,
            @Nullable RefundTrigger refundTrigger
    ) {
        super(statusBeforeRefunded);
        this.gatewayAuthData = gatewayAuthData;
        this.refundTrigger = refundTrigger;
    }

    /**
     * Enumeration of possible refund operation trigger
     */
    public enum RefundTrigger {
        /**
         * requested directly by the system flow in response to specific conditions
         */
        AUTOMATIC,
        /**
         * an operator explicitly initiates a refund
         */
        MANUAL
    }
}
