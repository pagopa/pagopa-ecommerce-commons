package it.pagopa.ecommerce.commons.documents.v2.authorization;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.net.URI;

/**
 * Extensible interface to handle multiple payment gateway authorization
 * requested data
 */
@JsonIgnoreProperties(
        value = "type", // ignore manually set detailType, it will be automatically generated by Jackson
        // during serialization
        allowSetters = true // allows the detailType to be set during deserialization
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = false)
@JsonSubTypes(
    {
            @JsonSubTypes.Type(value = PgsTransactionGatewayAuthorizationRequestedData.class, name = "PGS"),
            @JsonSubTypes.Type(value = NpgTransactionGatewayAuthorizationRequestedData.class, name = "NPG"),
    }
)
public sealed interface TransactionGatewayAuthorizationRequestedData permits NpgTransactionGatewayAuthorizationRequestedData,PgsTransactionGatewayAuthorizationRequestedData {

    /**
     * Authorization data type discriminator field enumeration
     */
    enum AuthorizationDataType {
        /**
         * PGS data type
         */
        PGS,
        /**
         * NPG data type
         */
        NPG
    }

    /**
     * Get discriminator field enumeration value
     *
     * @return the detail type enumeration value associated to the current detail
     *         instance
     */
    AuthorizationDataType getType();

    /**
     * Get brand logo
     *
     * @return the URI associated to the received brand logo
     */
    URI getLogo();
}