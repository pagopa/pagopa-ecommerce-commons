package it.pagopa.ecommerce.commons.documents.v2.activation;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotNull;

/**
 * Empty transaction activation data indicating no additional information are
 * available for the current transaction
 */
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class EmptyTransactionGatewayActivationData implements TransactionGatewayActivationData {

    @NotNull
    private static final ActivationDataType TYPE = ActivationDataType.EMPTY;

    @Override
    public ActivationDataType getType() {
        return TYPE;
    }
}
