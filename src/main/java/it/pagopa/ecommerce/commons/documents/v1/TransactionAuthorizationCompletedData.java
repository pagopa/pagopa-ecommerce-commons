package it.pagopa.ecommerce.commons.documents.v1;

import it.pagopa.ecommerce.commons.generated.server.model.AuthorizationResultDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.lang.Nullable;

import java.time.OffsetDateTime;

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
     * The rrn information
     */
    @Nullable
    private String rrn;

    /**
     * PGS authorization completion timestamp
     */
    private String timestampOperation;

    /**
     * The payment gateway authorization outcome
     */
    private AuthorizationResultDto authorizationResultDto;
}
