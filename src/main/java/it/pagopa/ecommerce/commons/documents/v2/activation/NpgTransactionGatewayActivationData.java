package it.pagopa.ecommerce.commons.documents.v2.activation;

import lombok.*;

import javax.validation.constraints.NotNull;

/**
 * Activation data associated for NPG gateway
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public final class NpgTransactionGatewayActivationData implements TransactionGatewayActivationData {

    private String orderId;

    private String correlationId;

    @NotNull
    private static final ActivationDataType TYPE = ActivationDataType.NPG;

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
