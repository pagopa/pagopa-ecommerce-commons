package it.pagopa.ecommerce.commons.domain;

import it.pagopa.ecommerce.commons.annotations.ValueObject;

import java.util.regex.Pattern;

/**
 * <p>An <abbr>RPT</abbr> id.</p>
 * <p>This identifies a single notice given to a citizen inside the pagoPA platform.</p>
 * <p>It <b>MUST</b> adhere to the following format: {@code ([a-zA-Z\d]{29})}</p>
 * @param value RPT id value
 */
@ValueObject
public record RptId(String value) {
    private static final Pattern rptIdRegex = Pattern.compile("([a-zA-Z\\d]{29})");

    /**
     * Construct an {@code RptId} from a {@link String} value.
     * @param value <abbr>RPT</abbr> id value
     * @throws IllegalArgumentException if the <abbr>RPT</abbr> id is not formally valid (see the class documentation for the format specification).
     */
    public RptId {
        if (!rptIdRegex.matcher(value).matches()) {
            throw new IllegalArgumentException("Ill-formed RPT id: " + value + ". Doesn't match format: " + rptIdRegex.pattern());
        }
    }

    public String getFiscalCode() {
        return value.substring(0, 11);
    }

    public String getNoticeId() {
        return value.substring(11);
    }

    public String getAuxDigit()  {
        return getNoticeId().substring(0,1);
    }

    public String getApplicationCode()  {
        return ("0").equals(getAuxDigit()) ? getNoticeId().substring(1,3) : null;
    }

    public String getIUV()  {
        return getNoticeId().substring(getApplicationCode() != null ? 3 : 1,18);
    }
}

