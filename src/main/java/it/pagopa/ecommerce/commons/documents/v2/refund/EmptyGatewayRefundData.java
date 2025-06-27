package it.pagopa.ecommerce.commons.documents.v2.refund;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Generated;

/**
 * Empty gateway refund data, used for gateway for which no additional data have
 * to be saved
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Generated
public final class EmptyGatewayRefundData implements GatewayRefundData {
    /**
     * Refund data type
     */
    private static final RefundDataType TYPE = RefundDataType.EMPTY;

    /**
     * No-args constructor
     */
    /*
     * @formatter:off
     *
     * Warning java:S1186 - Methods should not be empty
     * Suppressed because this constructor is required for gateway
     * refund data implementation and should remain empty
     *
     * @formatter:on
     */
    @SuppressWarnings("java:S1186")
    public EmptyGatewayRefundData() {
    }

    @Override
    public RefundDataType getType() {
        return TYPE;
    }
}
