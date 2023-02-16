package it.pagopa.ecommerce.commons.documents;

import it.pagopa.ecommerce.commons.generated.server.model.AuthorizationResultDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.lang.Nullable;

/**
 * Data related to authorization status update by a payment gateway
 */
@AllArgsConstructor
@Data
@Document
public class TransactionAuthorizationCompletedData {
    /**
     * The authorization code
     */
    @Nullable
    private String authorizationCode;
    /**
     * The payment gateway authorization outcome
     */
    private AuthorizationResultDto authorizationResultDto;
}
