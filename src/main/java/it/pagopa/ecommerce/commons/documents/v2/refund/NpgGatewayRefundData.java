package it.pagopa.ecommerce.commons.documents.v2.refund;

import lombok.*;

import jakarta.validation.constraints.NotNull;

/**
 * NPG gateway data for a refunded transaction
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Generated
public final class NpgGatewayRefundData implements GatewayRefundData {
    /**
     * Refund data type
     */
    private static final RefundDataType TYPE = RefundDataType.NPG;

    /**
     * Operation id received by NPG for a successfully refunded transaction
     */
    @NotNull
    private String operationId;

    /**
     * All-args constructor
     *
     * @param operationId the operation id
     */
    public NpgGatewayRefundData(String operationId) {
        this.operationId = operationId;
    }

    @Override
    public RefundDataType getType() {
        return TYPE;
    }
}
