package it.pagopa.ecommerce.commons.utils;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Either;
import it.pagopa.ecommerce.commons.exceptions.RedirectConfigurationException;
import it.pagopa.ecommerce.commons.exceptions.RedirectConfigurationType;
import it.pagopa.ecommerce.commons.utils.bean.redirect.configuration.RedirectUrlMappingCriteria;
import it.pagopa.ecommerce.commons.utils.bean.redirect.configuration.RedirectUrlMappingEntry;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Class used to handle redirect payment methods url configuration and search
 * logic based on input matching criteria
 */
public class RedirectUrlMappingConf {

    private final List<RedirectUrlMappingEntry> urlConfiguration;

    static final ObjectMapper objectMapper;

    static {
        // configure object mapper for deserialization
        objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES, true)
                .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true)
                .configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, true);

    }

    /**
     * Constructor
     *
     * @param urlConfigurationJsonValue         the URLs configuration value JSON
     *                                          representation
     * @param expectedMatchingCriteriaJsonValue the expected matching criteria to be
     *                                          honored by configuration - used to
     *                                          check that loaded configuration have
     *                                          the expected URLs configured for
     *                                          specific criteria
     */
    public RedirectUrlMappingConf(
            String urlConfigurationJsonValue,
            String expectedMatchingCriteriaJsonValue
    ) {

        try {
            urlConfiguration = objectMapper.readValue(urlConfigurationJsonValue, new TypeReference<>() {
            });
            List<Map<RedirectUrlMappingCriteria, String>> expectedMatchingCriteria = objectMapper
                    .readValue(expectedMatchingCriteriaJsonValue, new TypeReference<>() {
                    });
            expectedMatchingCriteria.forEach(
                    matchingCriteria -> getRedirectUrlForCriteria(matchingCriteria)
                            .fold(error -> {
                                throw new RedirectConfigurationException(
                                        "Redirect url configuration does not match expected criteria: %s"
                                                .formatted(error.getMessage()),
                                        RedirectConfigurationType.BACKEND_URLS
                                );
                            },
                                    Function.identity()
                            )
            );
        } catch (JacksonException e) {
            throw new RedirectConfigurationException(
                    "Invalid redirect url configuration: error parsing json values",
                    RedirectConfigurationType.BACKEND_URLS,
                    e
            );
        }

    }

    /**
     * Retrieve url configuration that matches input search criteria
     *
     * @param matchingCriteria - the search matching criteria
     * @return Either the fetched configuration or error in case of
     *         missing/ambiguous search result
     */
    public Either<RedirectConfigurationException, RedirectUrlMappingEntry> getRedirectUrlForCriteria(
                                                                                                     Map<RedirectUrlMappingCriteria, String> matchingCriteria
    ) {
        List<RedirectUrlMappingEntry> entries = urlConfiguration.stream()
                .filter(
                        confEntry -> matchingCriteria.entrySet().stream()
                                .allMatch(
                                        entry -> confEntry
                                                .matchingCriteria()
                                                // if the entry key is not present in the configuration matching
                                                // criteria we consider it as a match, otherwise we check if the value
                                                // matches
                                                .getOrDefault(entry.getKey(), entry.getValue())
                                                .equals(entry.getValue())
                                )
                )
                .toList();
        if (entries.size() != 1) {
            String errorMessageHeader = entries.isEmpty() ? "No configuration found"
                    : "Multiple configurations found: %s".formatted(entries);
            String errorMessage = errorMessageHeader
                    + " for the provided matching criteria: %s".formatted(matchingCriteria);
            return Either
                    .left(new RedirectConfigurationException(errorMessage, RedirectConfigurationType.BACKEND_URLS));
        }
        return Either.right(entries.getFirst());
    }

}
