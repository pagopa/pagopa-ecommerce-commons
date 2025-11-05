package it.pagopa.ecommerce.commons.documents.v1;

import com.fasterxml.jackson.annotation.JsonCreator;
import it.pagopa.ecommerce.commons.generated.server.model.AuthorizationResultDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.lang.Nullable;

import jakarta.validation.constraints.NotNull;

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
    @NotNull
    private String timestampOperation;

    /**
     * The PGS errorCode
     */
    @Nullable
    private String errorCode;

    /**
     * The payment gateway authorization outcome
     */
    private AuthorizationResultDto authorizationResultDto;

    @JsonCreator
    private TransactionAuthorizationCompletedData() {
    }
}
