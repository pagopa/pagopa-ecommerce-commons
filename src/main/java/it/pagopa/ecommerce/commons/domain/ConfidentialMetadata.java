package it.pagopa.ecommerce.commons.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.pagopa.ecommerce.commons.utils.ConfidentialDataManager;

import javax.annotation.Nonnull;

/**
 * <p>
 * A sealed interface for confidential metadata. Each metadata variant is
 * identified through a different {@link ConfidentialDataManager.Mode Mode}
 * </p>
 */
@JsonIgnoreProperties(value = "mode")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "mode", visible = true)
@JsonSubTypes(
    {
            @JsonSubTypes.Type(value = AESMetadata.class, name = "AES_GCM_NOPAD"),
            @JsonSubTypes.Type(value = AESMetadata.class, name = "AES_GCM_NOPAD_NOSALT"),
    }
)
public sealed interface ConfidentialMetadata permits AESMetadata {
    /**
     * Gets the mode identifying the metadata
     *
     * @return the mode
     */
    @Nonnull
    ConfidentialDataManager.Mode getMode();
}
