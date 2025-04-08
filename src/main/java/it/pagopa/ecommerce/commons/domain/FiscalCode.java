package it.pagopa.ecommerce.commons.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import it.pagopa.ecommerce.commons.annotations.ValueObject;
import it.pagopa.ecommerce.commons.utils.ConfidentialDataManager;

import javax.annotation.Nonnull;

/**
 * <p>
 * A value object holding an user fiscal code.
 * </p>
 *
 * @param value fiscal code
 */
@ValueObject
public record FiscalCode(String value)
        implements
        ConfidentialDataManager.ConfidentialData {

    /**
     * {@link FiscalCode} constructor.
     *
     * @param value fiscal code
     */
    public FiscalCode {
    }

    @Nonnull
    @JsonValue
    @Override
    public String toStringRepresentation() {
        return value;
    }

}
