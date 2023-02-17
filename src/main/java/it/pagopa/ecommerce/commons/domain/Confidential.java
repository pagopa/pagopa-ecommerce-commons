package it.pagopa.ecommerce.commons.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.pagopa.ecommerce.commons.utils.ConfidentialDataManager;

/**
 * <p>
 * An object holding confidential opaque data, along with the necessary metadata
 * to get the original data back.
 * </p>
 */
public record Confidential<T extends ConfidentialDataManager.ConfidentialData> (
        @JsonProperty("metadata") ConfidentialMetadata confidentialMetadata,
        @JsonProperty("data") String opaqueData
) {
    /**
     * Constructs a {@link Confidential} from existing data
     *
     * @param confidentialMetadata metadata about how this confidential data was
     *                             protected
     * @param opaqueData           opaque data (e.g. a ciphertext, a token to an
     *                             external service, etc.)
     */
    @JsonCreator
    public Confidential {
    }
}
