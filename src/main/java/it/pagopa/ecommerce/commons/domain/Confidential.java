package it.pagopa.ecommerce.commons.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.pagopa.ecommerce.commons.utils.ConfidentialDataManager;

/**
 * <p>
 * An object holding confidential data.
 * </p>
 */
public record Confidential<T extends ConfidentialDataManager.ConfidentialData> (
        @JsonProperty("metadata") ConfidentialMetadata confidentialMetadata,
        @JsonProperty("data") String encodedCipherText
) {
    @JsonCreator
    public Confidential {
    }
}
