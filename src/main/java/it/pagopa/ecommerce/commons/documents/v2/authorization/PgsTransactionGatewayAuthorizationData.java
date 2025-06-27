package it.pagopa.ecommerce.commons.documents.v2.authorization;

import it.pagopa.ecommerce.commons.generated.server.model.AuthorizationResultDto;
import lombok.*;
import org.springframework.lang.Nullable;

import jakarta.validation.constraints.NotNull;

/**
 * PGS transaction authorization completed data
 */
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public final class PgsTransactionGatewayAuthorizationData implements TransactionGatewayAuthorizationData {

    /**
     * The PGS errorCode
     */
    @Nullable
    private String errorCode;

    /**
     * The payment gateway authorization outcome
     */
    private AuthorizationResultDto authorizationResultDto;

    @NotNull
    private static final TransactionGatewayAuthorizationData.AuthorizationDataType TYPE = AuthorizationDataType.PGS;

    /**
     * All-args constructor
     *
     * @param errorCode              the error code
     * @param authorizationResultDto the authorization result
     */
    public PgsTransactionGatewayAuthorizationData(
            String errorCode,
            AuthorizationResultDto authorizationResultDto
    ) {
        this.errorCode = errorCode;
        this.authorizationResultDto = authorizationResultDto;
    }

    @Override
    public AuthorizationDataType getType() {
        return TYPE;
    }
}
