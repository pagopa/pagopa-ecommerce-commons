package it.pagopa.ecommerce.commons.documents.v2.authorization;

import lombok.*;

import javax.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.net.URI;

/**
 * NPG transaction authorization requested data
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
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

    /**
     * All-args constructor
     *
     * @param logo                    the logo URI
     * @param brand                   the brand
     * @param sessionId               the session id
     * @param confirmPaymentSessionId the confirmation payment session id
     * @param walletInfo              the wallet info
     */
    public NpgTransactionGatewayAuthorizationRequestedData(
            URI logo,
            String brand,
            String sessionId,
            String confirmPaymentSessionId,
            WalletInfo walletInfo
    ) {
        this.logo = logo;
        this.brand = brand;
        this.sessionId = sessionId;
        this.confirmPaymentSessionId = confirmPaymentSessionId;
        this.walletInfo = walletInfo;
    }

    @Override
    public AuthorizationDataType getType() {
        return AuthorizationDataType.NPG;
    }
}
