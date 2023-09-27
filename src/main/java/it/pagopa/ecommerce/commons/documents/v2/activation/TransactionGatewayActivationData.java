package it.pagopa.ecommerce.commons.documents.v2.activation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Extensible interface to handle multiple payment gateway transaction activated
 * data
 */
@JsonIgnoreProperties(
        value = "type", // ignore manually set detailType, it will be automatically generated by Jackson
        // during serialization
        allowSetters = true // allows the detailType to be set during deserialization
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = false)
@JsonSubTypes(
    {
            @JsonSubTypes.Type(value = EmptyTransactionGatewayActivationData.class, name = "EMPTY"),
            @JsonSubTypes.Type(value = NpgTransactionGatewayActivationData.class, name = "NPG"),
    }
)
public sealed interface TransactionGatewayActivationData permits EmptyTransactionGatewayActivationData,NpgTransactionGatewayActivationData {

    /**
     * Activation data type discriminator field enumeration
     */
    enum ActivationDataType {
        /**
         * Empty data type
         */
        EMPTY,
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
    ActivationDataType getType();
}
