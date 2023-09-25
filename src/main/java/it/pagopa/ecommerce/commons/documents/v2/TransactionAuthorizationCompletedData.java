package it.pagopa.ecommerce.commons.documents.v2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.pagopa.ecommerce.commons.documents.v2.authorization.TransactionGatewayAuthorizationData;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;

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
     * Authorization completion timestamp
     */
    @NotNull
    private String timestampOperation;

    @NotNull
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "_class", visible = false)
    private TransactionGatewayAuthorizationData transactionGatewayAuthorizationData;

    @JsonCreator
    private TransactionAuthorizationCompletedData() {
    }
}
