package it.pagopa.ecommerce.commons.documents.v2.activation;

import lombok.*;

import jakarta.validation.constraints.NotNull;

/**
 * Activation data associated for NPG gateway
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public final class NpgTransactionGatewayActivationData implements TransactionGatewayActivationData {

    private String orderId;

    private String correlationId;

    @NotNull
    private static final ActivationDataType TYPE = ActivationDataType.NPG;

    /**
     * All-args constructor
     *
     * @param orderId       the order id
     * @param correlationId the correlation id
     */
    public NpgTransactionGatewayActivationData(
            String orderId,
            String correlationId
    ) {
        this.orderId = orderId;
        this.correlationId = correlationId;
    }

    /**
     * Get discriminator field enumeration value
     *
     * @return the detail type enumeration value associated to the current detail
     *         instance
     */
    public ActivationDataType getType() {
        return TYPE;
    }
}
