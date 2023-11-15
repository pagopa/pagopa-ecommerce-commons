package it.pagopa.ecommerce.commons.utils;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.trace.Span;
import io.vavr.control.Either;
import it.pagopa.ecommerce.commons.client.NpgClient;
import it.pagopa.ecommerce.commons.exceptions.NpgApiKeyConfigurationException;
import it.pagopa.ecommerce.commons.exceptions.NpgApiKeyMissingPspRequested;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * This class take cares of parse NPG per PSP api key configuration json
 */
@Slf4j
public class NpgPspApiKeysConfig {

    private final Map<String, String> configuration;

    /**
     * Constructor
     *
     */
    NpgPspApiKeysConfig(
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
     * @param paymentMethod           - payment method for which api key have been
     *                                configured
     * @param objectMapper            - {@link ObjectMapper} used to parse input
     *                                JSON
     * @return either the parsed map or the related parsing exception
     */
    public static Either<NpgApiKeyConfigurationException, NpgPspApiKeysConfig> parseApiKeyConfiguration(
                                                                                                        String jsonSecretConfiguration,
                                                                                                        Set<String> pspToHandle,
                                                                                                        NpgClient.PaymentMethod paymentMethod,
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
                        new NpgApiKeyConfigurationException(
                                "Misconfigured api keys. Missing keys: %s".formatted(expectedKeys),
                                paymentMethod
                        )
                );
            }
            return Either.right(new NpgPspApiKeysConfig(apiKeys));
        } catch (JacksonException ignored) {
            // exception here is ignored on purpose in order to avoid secret configuration
            // logging in case of wrong configured json string object
            return Either.left(new NpgApiKeyConfigurationException("Invalid json configuration map", paymentMethod));
        }
    }

    /**
     * Retrieves an API key for a specific PSP
     *
     * @param psp the PSP you want the API key for
     * @return the API key corresponding to the input PSP
     */
    public Either<NpgApiKeyMissingPspRequested, String> get(String psp) {
        if (configuration.containsKey(psp)) {
            return Either.right(configuration.get(psp));
        } else {
            NpgApiKeyMissingPspRequested npgApiKeyMissingPspRequested = new NpgApiKeyMissingPspRequested(
                    psp,
                    configuration.keySet()
            );
            Span.current().recordException(npgApiKeyMissingPspRequested);

            return Either.left(npgApiKeyMissingPspRequested);
        }
    }
}
