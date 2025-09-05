package it.pagopa.ecommerce.commons.documents.v2.authorization;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.net.URI;

/**
 * Redirect transaction authorization requested data
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@ToString(callSuper = true)
public final class RedirectTransactionGatewayAuthorizationRequestedData
        implements TransactionGatewayAuthorizationRequestedData {
    /**
     * Logo URI associated to chosen transaction payment method
     */
    @NotNull
    private URI logo;

    /**
     * The timeout (in milliseconds), communicated by the PSP during redirection api
     * call, that the eCommerce b.e. has to wait for transaction outcome to be
     * received (max timeout)
     */
    private int transactionOutcomeTimeoutMillis;

    /**
     * All-args constructor
     *
     * @param logo                            the logo URI
     * @param transactionOutcomeTimeoutMillis the transaction outcome timeout in
     *                                        milliseconds
     */
    public RedirectTransactionGatewayAuthorizationRequestedData(
            URI logo,
            int transactionOutcomeTimeoutMillis
    ) {
        this.logo = logo;
        this.transactionOutcomeTimeoutMillis = transactionOutcomeTimeoutMillis;
    }

    @Override
    public AuthorizationDataType getType() {
        return AuthorizationDataType.REDIRECT;
    }
}
