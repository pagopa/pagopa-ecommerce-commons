package it.pagopa.ecommerce.commons.documents.v2.refund;

import lombok.*;

import javax.validation.constraints.NotNull;

/**
 * NPG gateway data for a refunded transaction
 */
@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
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

    @Override
    public RefundDataType getType() {
        return TYPE;
    }
}
