package it.pagopa.ecommerce.commons.documents.v2.authorization;

import lombok.*;

import java.net.URI;

/**
 * NPG transaction authorization requested data
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public final class NpgTransactionGatewayAuthorizationRequestedData
        implements TransactionGatewayAuthorizationRequestedData {

    private URI logo;

    private String brand;

    @Override
    public AuthorizationDataType getType() {
        return AuthorizationDataType.NPG;
    }
}
