package it.pagopa.ecommerce.commons.documents.v2.activation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * NPG transaction activation data
 */
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public final class NpgTransactionGatewayActivationData implements TransactionGatewayActivationData {

    private String orderId;

    private String correlationId;

    private String sessionId;
}
