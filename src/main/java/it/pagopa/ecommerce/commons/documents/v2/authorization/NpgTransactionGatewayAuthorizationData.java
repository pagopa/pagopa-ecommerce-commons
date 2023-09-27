package it.pagopa.ecommerce.commons.documents.v2.authorization;

import it.pagopa.ecommerce.commons.generated.npg.v1.dto.OperationResultDto;
import lombok.*;

/**
 * NPG transaction authorization completed data
 */
@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
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

    private static final TransactionGatewayAuthorizationData.AuthorizationDataType TYPE = AuthorizationDataType.NPG;

    @Override
    public AuthorizationDataType getType() {
        return TYPE;
    }
}
