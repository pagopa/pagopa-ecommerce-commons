package it.pagopa.ecommerce.commons.domain;

import it.pagopa.ecommerce.commons.annotations.ValueObject;
import org.springframework.data.annotation.PersistenceConstructor;

import java.util.regex.Pattern;

/**
 * <p>
 * An idempotency key used to identify payment activation requests towards Nodo.
 * </p>
 * <p>
 * It <b>MUST</b> have the following format: {@code \d{11}_[a-zA-Z\d]{10}} where
 * the first 11 digits are the <abbr>PSP</abbr> fiscal code and the latest 10
 * characters are a key identifier that ensures uniqueness.
 * </p>
 *
 * @param rawValue raw value of the idempotency key
 */
@ValueObject
public record IdempotencyKey(String rawValue) {
    private static final Pattern pspFiscalCodeRegex = Pattern.compile("\\d{11}");
    private static final Pattern keyIdentifierRegex = Pattern.compile("[a-zA-Z\\d]{10}");

    /**
     * <p>
     * Construct an {@link IdempotencyKey} from the two components, a
     * <abbr>PSP</abbr> fiscal code and a key identifier
     * </p>
     *
     * @param pspFiscalCode PSP fiscal code
     * @param keyIdentifier Key identifier
     * @throws IllegalArgumentException if the key is not formally valid (see class
     *                                  documentation for the format)
     */
    public IdempotencyKey(
            String pspFiscalCode,
            String keyIdentifier
    ) {
        this(pspFiscalCode + "_" + keyIdentifier);
    }

    /**
     * Construct an {@link IdempotencyKey} from a raw {@link String} value.
     *
     * @param rawValue the raw idempotency key
     * @throws IllegalArgumentException if rawValue is not formally valid (see class
     *                                  documentation for the format)
     */
    @PersistenceConstructor
    public IdempotencyKey {
        String[] matches = rawValue.split("_");

        if (matches.length != 2) {
            throw new IllegalArgumentException("Key doesn't match format `$pspFiscalCode_$keyIdentifier`");
        }

        String pspFiscalCode = matches[0];
        String keyIdentifier = matches[1];

        if (!pspFiscalCodeRegex.matcher(pspFiscalCode).matches()) {
            throw new IllegalArgumentException("PSP fiscal code doesn't match regex: " + pspFiscalCodeRegex.pattern());
        }

        if (!keyIdentifierRegex.matcher(keyIdentifier).matches()) {
            throw new IllegalArgumentException("Key identifier doesn't match regex: " + keyIdentifierRegex.pattern());
        }
    }
}
