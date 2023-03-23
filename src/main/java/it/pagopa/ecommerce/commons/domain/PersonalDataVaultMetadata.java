package it.pagopa.ecommerce.commons.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import it.pagopa.ecommerce.commons.utils.ConfidentialDataManager;

import javax.annotation.Nonnull;

/**
 * <p>
 * Metadata class for confidential data encrypted through the Personal Data
 * Vault service.
 * </p>
 * <p>
 * This record is empty on purpose, as no additional data is needed to decrypt
 * secrets (other than the received token).
 * </p>
 */
@JsonIgnoreProperties(value = "mode")
public record PersonalDataVaultMetadata()
        implements
        ConfidentialMetadata {
    @Nonnull
    @Override
    public ConfidentialDataManager.Mode getMode() {
        return ConfidentialDataManager.Mode.PERSONAL_DATA_VAULT;
    }
}
