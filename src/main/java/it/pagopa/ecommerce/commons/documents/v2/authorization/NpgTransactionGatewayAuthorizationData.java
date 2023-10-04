package it.pagopa.ecommerce.commons.documents.v2.authorization;

import it.pagopa.ecommerce.commons.generated.npg.v1.dto.OperationResultDto;
import lombok.*;

import java.net.URI;

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

    /**
     * NPG received payment circuit value
     */
    private String paymentCircuit;

    /**
     * Logo URI associated to received payment circuit
     */
    private URI logo;

    private static final TransactionGatewayAuthorizationData.AuthorizationDataType TYPE = AuthorizationDataType.NPG;

    @Override
    public AuthorizationDataType getType() {
        return TYPE;
    }
}
