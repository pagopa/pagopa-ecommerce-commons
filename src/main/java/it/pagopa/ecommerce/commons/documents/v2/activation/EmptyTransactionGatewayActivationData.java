package it.pagopa.ecommerce.commons.documents.v2.activation;

import lombok.*;
import lombok.experimental.FieldDefaults;

import jakarta.validation.constraints.NotNull;

/**
 * Empty transaction activation data indicating no additional information are
 * available for the current transaction
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class EmptyTransactionGatewayActivationData implements TransactionGatewayActivationData {

    @NotNull
    private static final ActivationDataType TYPE = ActivationDataType.EMPTY;

    /**
     * No-args constructor
     */
    /*
     * @formatter:off
     *
     * Warning java:S1186 - Methods should not be empty
     * Suppressed because this constructor is required for gateway
     * activation data implementation and should remain empty
     *
     * @formatter:on
     */
    @SuppressWarnings("java:S1186")
    public EmptyTransactionGatewayActivationData() {
    }

    @Override
    public ActivationDataType getType() {
        return TYPE;
    }
}
