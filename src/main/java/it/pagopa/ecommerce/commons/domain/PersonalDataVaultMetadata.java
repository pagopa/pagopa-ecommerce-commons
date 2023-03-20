package it.pagopa.ecommerce.commons.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import it.pagopa.ecommerce.commons.utils.ConfidentialDataManager;

import javax.annotation.Nonnull;

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
