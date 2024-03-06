package it.pagopa.ecommerce.commons.documents.v2.authorization;

import lombok.*;

import javax.validation.constraints.NotNull;
import java.net.URI;

/**
 * Redirect transaction authorization requested data
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
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
     * The payment method type value
     */
    private PaymentMethodType paymentMethodType;

    /**
     * Enumeration of all redirect supported payment method type
     */
    public enum PaymentMethodType {
        /**
         * Bank account payment
         */
        BANK_ACCOUNT
    }

    @Override
    public AuthorizationDataType getType() {
        return AuthorizationDataType.REDIRECT;
    }
}
