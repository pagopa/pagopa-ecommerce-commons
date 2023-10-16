package it.pagopa.ecommerce.commons.documents.v2.authorization;

import lombok.*;

import javax.validation.constraints.NotNull;
import java.net.URI;

/**
 * NPG transaction authorization requested data
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public final class NpgTransactionGatewayAuthorizationRequestedData
        implements TransactionGatewayAuthorizationRequestedData {
    /**
     * Logo URI associated to chosen transaction payment method
     */
    @NotNull
    private URI logo;
    /**
     * Npg brand value
     */
    @NotNull
    private String brand;
    /**
     * NPG confirm payment received sessionId
     */
    @NotNull
    private String authorizationSessionId;

    @Override
    public AuthorizationDataType getType() {
        return AuthorizationDataType.NPG;
    }
}
