package it.pagopa.ecommerce.commons.utils;

import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class RedirectConfigurationKeysConfig {

    private final Map<String, URI> redirectBeApiCallUriMap;

    public RedirectConfigurationKeysConfig(Map<String, URI> redirectBeApiCallUriMap) {
        this.redirectBeApiCallUriMap = redirectBeApiCallUriMap;
    }

    /**
     * Execute a recursive search on the redirectBeApiCallUriMap. The recursion
     * method will be called with the key without the first parameter element.
     *
     * @param params List of parameters that compose the key.
     * @return The found URI or an empty value.
     */
    public Optional<URI> searchRedirectUrlForPsp(String... params) {

        String key = String.join("-", params);
        if (redirectBeApiCallUriMap.containsKey(key)) {
            return Optional.of(redirectBeApiCallUriMap.get(key));
        }
        if (params.length > 1) {
            return searchRedirectUrlForPsp(Arrays.copyOfRange(params, 1, params.length));
        }
        return Optional.empty();
    }

    public Map<String, URI> getRedirectBeApiCallUriMap() {
        return redirectBeApiCallUriMap;
    }
}
