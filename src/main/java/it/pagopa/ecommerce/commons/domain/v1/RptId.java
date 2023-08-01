package it.pagopa.ecommerce.commons.domain.v1;

import it.pagopa.ecommerce.commons.annotations.ValueObject;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.regex.Pattern;

/**
 * An <abbr>RPT</abbr> id.
 *
 * <p>
 * This identifies a single notice given to a citizen inside the pagoPA
 * platform.
 *
 * <p>
 * It <b>MUST</b> adhere to the following format: {@code ([a-zA-Z\d]{29})}
 *
 * @param value RPT id value
 */
@ValueObject
public record RptId(String value) {
    private static final Pattern rptIdRegex = Pattern.compile("([0-9]{29})");

    /**
     * Construct an {@code RptId} from a {@link String} value.
     *
     * @param value <abbr>RPT</abbr> id value
     * @throws IllegalArgumentException if the <abbr>RPT</abbr> id is not formally
     *                                  valid (see the class documentation for the
     *                                  format specification).
     */
    public RptId {
        if (!rptIdRegex.matcher(value).matches()) {
            throw new IllegalArgumentException(
                    "Ill-formed RPT id: " + value + ". Doesn't match format: " + rptIdRegex.pattern()
            );
        }
    }

    /**
     * Get the fiscal code portion of the RPT id
     *
     * @return the fiscal code
     */
    public @NonNull String getFiscalCode() {
        return value.substring(0, 11);
    }

    /**
     * Get the notice id portion of the RPT id
     *
     * @return the notice id
     */
    public @NonNull String getNoticeId() {
        return value.substring(11);
    }

    /**
     * Get the auxiliary digit of the RPT id
     *
     * @return the auxiliary digit
     */
    public @NonNull String getAuxDigit() {
        return getNoticeId().substring(0, 1);
    }

    /**
     * Get the application code portion of the RPT id.
     *
     * @return the application code. Returns null if the auxiliary digit is
     *         different from 0.
     */
    public @Nullable String getApplicationCode() {
        return ("0").equals(getAuxDigit()) ? getNoticeId().substring(1, 3) : null;
    }

    /**
     * Get the IUV (Indicatore Univoco di Versamento) portion of the RPT id
     *
     * @return the IUV
     */
    public @NonNull String getIUV() {
        return getNoticeId().substring(getApplicationCode() != null ? 3 : 1, 18);
    }
}
