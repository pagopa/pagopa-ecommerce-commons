package it.pagopa.ecommerce.commons.documents.v2.authorization;

import lombok.*;

import javax.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

/**
 * Redirect transaction authorization completed data
 */
@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
public final class RedirectTransactionGatewayAuthorizationData implements TransactionGatewayAuthorizationData {

    private static final AuthorizationDataType TYPE = AuthorizationDataType.REDIRECT;

    /**
     * Redirect transaction authorization outcome enumeration
     */
    public enum Outcome {
        /**
         * Authorization completed successfully
         */
        OK,
        /**
         * Authorization cannot be completed (i.e. insufficient funds)
         */
        KO,
        /**
         * User cancel authorization process
         */
        CANCELED,
        /**
         * Authorization process did not complete before PSP expiration timeout
         */
        EXPIRED,
        /**
         * An error occurred during authorization process
         */
        ERROR
    }

    /**
     * Authorization outcome
     */
    @NotNull
    private Outcome outcome;
    /**
     * Authorization error code
     */
    @Nullable
    private String errorCode;

    @Override
    public AuthorizationDataType getType() {
        return TYPE;
    }
}
