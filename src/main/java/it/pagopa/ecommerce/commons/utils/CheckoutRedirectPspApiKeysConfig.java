package it.pagopa.ecommerce.commons.utils;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.trace.Span;
import io.vavr.control.Either;
import it.pagopa.ecommerce.commons.exceptions.CheckoutRedirectConfigurationException;
import it.pagopa.ecommerce.commons.exceptions.CheckoutRedirectMissingPspRequestedException;
import it.pagopa.ecommerce.commons.exceptions.CheckoutRedirectConfigurationType;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * This class take cares of parsing PSP api keys configuration for Checkout
 * Redirect
 */
@Slf4j
public class CheckoutRedirectPspApiKeysConfig {

    private final Map<String, String> configuration;

    /**
     * Constructor
     *
     */
    CheckoutRedirectPspApiKeysConfig(
            Map<String, String> configuration
    ) {
        this.configuration = Collections.unmodifiableMap(configuration);
    }

    /**
     * Return a map where valued with each psp id - api keys entries
     *
     * @param jsonSecretConfiguration - secret configuration json representation
     * @param pspToHandle             - psp expected to be present into
     *                                configuration json
     * @param objectMapper            - {@link ObjectMapper} used to parse input
     *                                JSON
     * @return either the parsed map or the related parsing exception
     */
    public static Either<CheckoutRedirectConfigurationException, CheckoutRedirectPspApiKeysConfig> parseApiKeyConfiguration(
                                                                                                                            String jsonSecretConfiguration,
                                                                                                                            Set<String> pspToHandle,
                                                                                                                            ObjectMapper objectMapper
    ) {
        try {
            Set<String> expectedKeys = new HashSet<>(pspToHandle);
            Map<String, String> apiKeys = objectMapper
                    .readValue(jsonSecretConfiguration, new TypeReference<HashMap<String, String>>() {
                    });
            Set<String> configuredKeys = apiKeys.keySet();
            expectedKeys.removeAll(configuredKeys);
            if (!expectedKeys.isEmpty()) {
                return Either.left(
                        new CheckoutRedirectConfigurationException(
                                "Misconfigured api keys. Missing keys: %s".formatted(expectedKeys),
                                CheckoutRedirectConfigurationType.API_KEYS
                        )
                );
            }
            return Either.right(new CheckoutRedirectPspApiKeysConfig(apiKeys));
        } catch (JacksonException ignored) {
            // exception here is ignored on purpose in order to avoid secret configuration
            // logging in case of wrong configured json string object
            return Either.left(
                    new CheckoutRedirectConfigurationException(
                            "Invalid json configuration map",
                            CheckoutRedirectConfigurationType.API_KEYS
                    )
            );
        }
    }

    /**
     * Retrieves an API key for a specific PSP
     *
     * @param psp the PSP you want the API key for
     * @return the API key corresponding to the input PSP
     */
    public Either<CheckoutRedirectMissingPspRequestedException, String> get(String psp) {
        if (configuration.containsKey(psp)) {
            return Either.right(configuration.get(psp));
        } else {
            CheckoutRedirectMissingPspRequestedException checkoutRedirectMissingPspRequestedException = new CheckoutRedirectMissingPspRequestedException(
                    psp,
                    configuration.keySet(),
                    CheckoutRedirectConfigurationType.API_KEYS
            );
            Span.current().recordException(checkoutRedirectMissingPspRequestedException);

            return Either.left(checkoutRedirectMissingPspRequestedException);
        }
    }
}
