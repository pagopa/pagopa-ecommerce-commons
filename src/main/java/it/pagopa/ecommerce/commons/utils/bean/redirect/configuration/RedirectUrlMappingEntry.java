package it.pagopa.ecommerce.commons.utils.bean.redirect.configuration;

import java.net.URI;
import java.util.Map;

/**
 * Redirect url configuration entry
 *
 * @param url              - the redirect url
 * @param matchingCriteria - the matching criteria configured for the url
 */
public record RedirectUrlMappingEntry(
        URI url,
        Map<String, String> matchingCriteria
) {
}
