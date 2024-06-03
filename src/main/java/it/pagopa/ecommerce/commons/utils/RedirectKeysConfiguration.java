package it.pagopa.ecommerce.commons.utils;

import io.vavr.control.Either;
import it.pagopa.ecommerce.commons.exceptions.RedirectConfigurationException;
import it.pagopa.ecommerce.commons.exceptions.RedirectConfigurationType;
import lombok.Getter;

import java.net.URI;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This class manages the PSP redirect URL map.
 */
@Getter
public class RedirectKeysConfiguration {

    private final Map<String, URI> redirectBeApiCallUriMap;

    /**
     * Constructor
     *
     * @param pspUrlMapping       - configuration parameter that contains PSP to URI
     *                            mapping
     * @param paymentTypeCodeList - set of all redirect payment type codes to be
     *                            handled flow
     */
    public RedirectKeysConfiguration(
            Map<String, String> pspUrlMapping,
            Set<String> paymentTypeCodeList
    ) {
        final Map<String, URI> redirectUriMap = new HashMap<>();
        // URI.create throws IllegalArgumentException that will prevent module load for
        // invalid PSP URI configuration
        pspUrlMapping.forEach(
                (
                 pspId,
                 uri
                ) -> redirectUriMap.put(pspId, URI.create(uri))
        );
        Set<String> missingKeys = paymentTypeCodeList
                .stream()
                .filter(Predicate.not(redirectUriMap::containsKey))
                .collect(Collectors.toSet());
        if (!missingKeys.isEmpty()) {
            throw new RedirectConfigurationException(
                    "Misconfigured redirect.pspUrlMapping, the following redirect payment type code b.e. URIs are not configured: %s"
                            .formatted(missingKeys),
                    RedirectConfigurationType.BACKEND_URLS
            );
        }
        this.redirectBeApiCallUriMap = Collections.unmodifiableMap(redirectUriMap);
    }

    /**
     * Returns a PSP redirect URL given a key by searching among the configured
     * URLs.
     *
     * @param touchpoint      - the touchpoint used to initiate the transaction
     * @param pspId           - the psp id chosen for the current
     * @param paymentTypeCode - redirect payment type code
     * @return Either valued with a PSP redirect URI, if valid, or exception for
     *         invalid key param or bad configuration
     */
    public Either<RedirectConfigurationException, URI> getRedirectUrlForPsp(
                                                                            String touchpoint,
                                                                            String pspId,
                                                                            String paymentTypeCode
    ) {

        /*
         * Search for the key touchpoint-paymentTypeCode-pspId in the redirectUrlMap. If
         * the key is not found, the method searches for paymentTypeCode-pspId, and if
         * not found, it searches for pspId.
         */
        Optional<URI> searchResult = searchRedirectUrlForPsp(touchpoint, pspId, paymentTypeCode);
        return searchResult.<Either<RedirectConfigurationException, URI>>map(Either::right).orElseGet(
                () -> Either.left(
                        new RedirectConfigurationException(
                                "Missing key for redirect return url with following search parameters: touchpoint: [%s] pspId: [%s] paymentTypeCode: [%s]"
                                        .formatted(
                                                touchpoint,
                                                pspId,
                                                paymentTypeCode
                                        ),
                                RedirectConfigurationType.BACKEND_URLS
                        )
                )
        );
    }

    /**
     * Execute a recursive search on the redirectBeApiCallUriMap. The recursion
     * method will be called with the key without the first parameter element.
     *
     * @param params List of parameters that compose the key.
     * @return The found URI or an empty value.
     */
    private Optional<URI> searchRedirectUrlForPsp(String... params) {
        String key = String.join("-", params);
        if (redirectBeApiCallUriMap.containsKey(key)) {
            return Optional.of(redirectBeApiCallUriMap.get(key));
        }
        if (params.length > 1) {
            return searchRedirectUrlForPsp(Arrays.copyOfRange(params, 1, params.length));
        }
        return Optional.empty();
    }

}
