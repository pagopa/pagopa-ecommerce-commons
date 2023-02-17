package it.pagopa.ecommerce.commons.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.pagopa.ecommerce.commons.utils.ConfidentialDataManager;

import javax.annotation.Nonnull;

@JsonIgnoreProperties(value = "mode")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "mode", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = AESMetadata.class, name = "AES/GCM/NoPadding"),
})
public sealed interface ConfidentialMetadata permits AESMetadata {
    @Nonnull ConfidentialDataManager.Mode getMode();
}
