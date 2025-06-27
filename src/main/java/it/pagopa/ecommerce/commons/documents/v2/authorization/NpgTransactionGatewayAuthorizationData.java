package it.pagopa.ecommerce.commons.documents.v2.authorization;

import it.pagopa.ecommerce.commons.generated.npg.v1.dto.OperationResultDto;
import lombok.*;
import org.springframework.lang.Nullable;

/**
 * NPG transaction authorization completed data
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@ToString(callSuper = true)
public final class NpgTransactionGatewayAuthorizationData implements TransactionGatewayAuthorizationData {

    /**
     * NPG operation result
     */
    private OperationResultDto operationResult;

    /**
     * NPG operation id
     */
    private String operationId;

    /**
     * NPG payment end to end id
     */
    private String paymentEndToEndId;

    /**
     * NPG authorization error code
     */
    @Nullable
    private String errorCode;

    /**
     * NPG additionalData.validationServiceId optional field
     */
    @Nullable
    private String validationServiceId;

    private static final TransactionGatewayAuthorizationData.AuthorizationDataType TYPE = AuthorizationDataType.NPG;

    /**
     * All-args constructor
     *
     * @param operationResult     the operation result
     * @param operationId         the operation id
     * @param paymentEndToEndId   the payment end to end id
     * @param errorCode           the authorization error code
     * @param validationServiceId the validation service id
     */
    public NpgTransactionGatewayAuthorizationData(
            OperationResultDto operationResult,
            String operationId,
            String paymentEndToEndId,
            String errorCode,
            String validationServiceId
    ) {
        this.operationResult = operationResult;
        this.operationId = operationId;
        this.paymentEndToEndId = paymentEndToEndId;
        this.errorCode = errorCode;
        this.validationServiceId = validationServiceId;
    }

    @Override
    public AuthorizationDataType getType() {
        return TYPE;
    }
}
