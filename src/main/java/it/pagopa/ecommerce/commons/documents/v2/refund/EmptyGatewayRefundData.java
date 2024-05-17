package it.pagopa.ecommerce.commons.documents.v2.refund;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Generated;
import lombok.NoArgsConstructor;

/**
 * Empty gateway refund data, used for gateway for which no additional data have
 * to be saved
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Generated
public final class EmptyGatewayRefundData implements GatewayRefundData {
    /**
     * Refund data type
     */
    private static final RefundDataType TYPE = RefundDataType.EMPTY;

    @Override
    public RefundDataType getType() {
        return TYPE;
    }
}
