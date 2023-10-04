package it.pagopa.ecommerce.commons.documents.v2.authorization;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Empty transaction authorization requested data
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@ToString(callSuper = true)
public final class EmptyTransactionGatewayAuthorizationRequestedData
        implements TransactionGatewayAuthorizationRequestedData {
    @Override
    public AuthorizationDataType getType() {
        return null;
    }
}
