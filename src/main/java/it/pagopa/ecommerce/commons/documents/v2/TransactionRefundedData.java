package it.pagopa.ecommerce.commons.documents.v2;

import it.pagopa.ecommerce.commons.documents.v2.refund.GatewayRefundData;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Generated;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotNull;

/**
 * Data related to a refunded transaction
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Document
@NoArgsConstructor
@Generated
public final class TransactionRefundedData extends BaseTransactionRefundedData {
    /**
     * Refund operation gateway data
     */
    @NotNull
    private GatewayRefundData gatewayOperationData;

    /**
     * Constructor
     *
     * @param gatewayOperationData transaction specific gateway data
     * @param statusBeforeRefunded the transaction status before refund operation
     */
    public TransactionRefundedData(
            @NotNull GatewayRefundData gatewayOperationData,
            @NotNull TransactionStatusDto statusBeforeRefunded
    ) {
        super(statusBeforeRefunded);
        this.gatewayOperationData = gatewayOperationData;
    }

}
