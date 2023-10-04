package it.pagopa.ecommerce.commons.documents.v2.authorization;

import lombok.*;
import org.springframework.lang.Nullable;

import java.net.URI;

/**
 * Empty transaction authorization requested data
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public final class NpgTransactionGatewayAuthorizationRequestedData
        implements TransactionGatewayAuthorizationRequestedData {

    private URI logo;

    @Nullable
    private String brand;

    @Override
    public AuthorizationDataType getType() {
        return AuthorizationDataType.NPG;
    }
}
