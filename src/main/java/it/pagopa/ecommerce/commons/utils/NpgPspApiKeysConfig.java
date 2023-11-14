package it.pagopa.ecommerce.commons.utils;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Either;
import it.pagopa.ecommerce.commons.client.NpgClient;
import it.pagopa.ecommerce.commons.exceptions.NpgApiKeyConfigurationException;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class take cares of parse NPG per PSP api key configuration json
 */
@Slf4j
public class NpgPspApiKeysConfig {

    /**
     * Object mapper instance used to parse Json api keys representation
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Secret json configuration
     */
    private final String jsonSecretConfiguration;

    /**
     * Set of all PSP expected to be present into configuration
     */
    private final Set<String> pspToHandle;

    /**
     * Payment method associated to the secret configuration
     */
    private final NpgClient.PaymentMethod paymentMethod;

    /**
     * Constructor
     *
     * @param jsonSecretConfiguration - secret configuration json representation
     * @param pspToHandle             - psp expected to be present into
     *                                configuration json
     * @param paymentMethod           - payment method for which api key have been
     *                                configured
     */
    public NpgPspApiKeysConfig(
            String jsonSecretConfiguration,
            Set<String> pspToHandle,
            NpgClient.PaymentMethod paymentMethod
    ) {
        this.jsonSecretConfiguration = jsonSecretConfiguration;
        this.pspToHandle = pspToHandle;
        this.paymentMethod = paymentMethod;
    }

    /**
     * Return a map where valued with each psp id - api keys entries
     *
     * @return either the parsed map or the related parsing exception
     */
    public Either<NpgApiKeyConfigurationException, Map<String, String>> parseApiKeyConfiguration() {
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
            return Either.right(apiKeys);
        } catch (JacksonException ignored) {
            // exception here is ignored on purpose in order to avoid secret configuration
            // logging in case of wrong configured json string object
            return Either.left(new NpgApiKeyConfigurationException("Invalid json configuration map", paymentMethod));
        }
    }

}
