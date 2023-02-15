package it.pagopa.ecommerce.commons.documents;

import it.pagopa.ecommerce.commons.generated.server.model.AuthorizationResultDto;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Data related to authorization status update by a payment gateway
 */
@AllArgsConstructor
@Data
@Document
public class TransactionAuthorizedData {
    /**
     * The authorization code
     */
    private String authorizationCode;
    /**
     * The payment gateway authorization outcome
     */
    private String authorizationOutcome;
}
