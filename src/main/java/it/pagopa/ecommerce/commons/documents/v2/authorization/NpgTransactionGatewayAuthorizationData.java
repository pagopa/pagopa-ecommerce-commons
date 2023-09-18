package it.pagopa.ecommerce.commons.documents.v2.authorization;

import it.pagopa.ecommerce.commons.generated.npg.v1.dto.OperationResultDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * NPG transaction authorization completed data
 */
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class NpgTransactionGatewayAuthorizationData implements TransactionGatewayAuthorizationData {

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

}
