package it.pagopa.ecommerce.commons.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import it.pagopa.ecommerce.commons.annotations.ValueObject;
import it.pagopa.ecommerce.commons.utils.ConfidentialDataManager;

import javax.annotation.Nonnull;
import java.util.regex.Pattern;

/**
 * <p>
 * A value object holding a valid fiscal code address.
 * </p>
 *
 * @param value fiscal code address
 */
@ValueObject
public record FiscalCode(String value)
        implements
        ConfidentialDataManager.ConfidentialData {

    /**
     * {@link FiscalCode} constructor. Validates the input fiscal code.
     *
     * @param value fiscal code address
     */
    public FiscalCode { }

    @Nonnull
    @JsonValue
    @Override
    public String toStringRepresentation() {
        return value;
    }

}
