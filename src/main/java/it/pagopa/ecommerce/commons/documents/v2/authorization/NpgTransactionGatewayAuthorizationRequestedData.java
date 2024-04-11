package it.pagopa.ecommerce.commons.documents.v2.authorization;

import lombok.*;

import javax.annotation.Nullable;
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
     * NPG session id
     */
    @NotNull
    private String sessionId;
    /**
     * NPG confirm payment received sessionId
     */
    private String confirmPaymentSessionId;

    /**
     * Details about the wallet used to perform authorization
     */
    @Nullable
    private WalletInfo walletInfo;

    @Override
    public AuthorizationDataType getType() {
        return AuthorizationDataType.NPG;
    }
}
