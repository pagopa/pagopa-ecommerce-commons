package it.pagopa.ecommerce.commons.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Either;
import it.pagopa.ecommerce.commons.client.NpgClient;
import it.pagopa.ecommerce.commons.exceptions.NpgApiKeyConfigurationException;

import java.util.*;
import java.util.function.Function;

/**
 * This class handles all NPG api keys such as default one and per methods psp.
 * Use {@link Builder} class to initialize a new class instance api keys
 */
public class NpgApiKeyConfiguration {

    /**
     * NpgApiKeyConfiguration builder class
     */
    public static class Builder {
        private String defaultApiKey;
        private final Map<NpgClient.PaymentMethod, NpgPspApiKeysConfig> methodsApiKeyMapping = new EnumMap<>(
                NpgClient.PaymentMethod.class
        );

        /**
         * Set the default NPG api key
         *
         * @param defaultApiKey the default api key to be set
         * @return this builder instance
         */
        public Builder setDefaultApiKey(String defaultApiKey) {
            this.defaultApiKey = defaultApiKey;
            return this;
        }

        /**
         * Add specific {@link NpgPspApiKeysConfig} configuration for input payment
         * method
         *
         * @param paymentMethod       the payment method for which register npg api keys
         *                            configuration
         * @param npgPspApiKeysConfig the npg api keys config
         * @return this builder instance
         * @throws NpgApiKeyConfigurationException if `paymentMethod` was already mapped
         */
        public Builder withMethodPspMapping(
                                            NpgClient.PaymentMethod paymentMethod,
                                            NpgPspApiKeysConfig npgPspApiKeysConfig
        ) {
            if (this.methodsApiKeyMapping.containsKey(paymentMethod)) {
                throw new NpgApiKeyConfigurationException(
                        "Api key mapping already registered for payment method: [%s]".formatted(paymentMethod)
                );
            }
            this.methodsApiKeyMapping.put(paymentMethod, npgPspApiKeysConfig);
            return this;
        }

        /**
         * Add specific {@link NpgPspApiKeysConfig} configuration for input payment
         * method. This method is the same as
         * {@link #withMethodPspMapping(NpgClient.PaymentMethod, NpgPspApiKeysConfig)}
         * one but parse psp api keys configuration with the given input information.
         * Input configuration is parsed using
         * {@link NpgPspApiKeysConfig#parseApiKeyConfiguration(String, Set, NpgClient.PaymentMethod, ObjectMapper)}
         * method
         *
         * @param paymentMethod           the payment methods to be added to mapping
         * @param jsonSecretConfiguration the json api keys mapping string
         *                                representation
         * @param pspToHandle             the psp expected to be handled
         * @param objectMapper            the object mapper instance to be used to parse
         *                                api keys configuration
         * @return this builder instance
         * @throws NpgApiKeyConfigurationException in case of error parsing psp api keys
         *                                         configuration
         */
        public Builder withMethodPspMapping(
                                            NpgClient.PaymentMethod paymentMethod,
                                            String jsonSecretConfiguration,
                                            Set<String> pspToHandle,
                                            ObjectMapper objectMapper
        ) {
            withMethodPspMapping(
                    paymentMethod,
                    NpgPspApiKeysConfig
                            .parseApiKeyConfiguration(jsonSecretConfiguration, pspToHandle, paymentMethod, objectMapper)
                            .fold(
                                    exception -> {
                                        throw exception;
                                    },
                                    Function.identity()
                            )
            );
            return this;
        }

        /**
         * Build {@link NpgApiKeyConfiguration} handler instance with the given api keys
         * configurations
         *
         * @return a new {@link NpgApiKeyConfiguration} instance or throw an error if
         *         configuration are not correct
         */
        public NpgApiKeyConfiguration build() {
            return new NpgApiKeyConfiguration(defaultApiKey, methodsApiKeyMapping);
        }
    }

    private final String defaultApiKey;

    private final Map<NpgClient.PaymentMethod, NpgPspApiKeysConfig> methodsApiKeyMapping;

    /**
     * Constructor
     *
     * @param defaultApiKey        NPG default api key
     * @param methodsApiKeyMapping the payment method psp api key mapping
     */
    NpgApiKeyConfiguration(
            String defaultApiKey,
            Map<NpgClient.PaymentMethod, NpgPspApiKeysConfig> methodsApiKeyMapping
    ) {
        if (Objects.isNull(defaultApiKey)) {
            throw new NpgApiKeyConfigurationException(
                    "Invalid configuration detected! Default api key mapping cannot be null"
            );
        }
        if (Objects.isNull(methodsApiKeyMapping) || methodsApiKeyMapping.isEmpty()) {
            throw new NpgApiKeyConfigurationException(
                    "Invalid configuration detected! Payment methods api key mapping cannot be null or empty"
            );
        }
        this.defaultApiKey = defaultApiKey;
        this.methodsApiKeyMapping = Collections.unmodifiableMap(methodsApiKeyMapping);
    }

    /**
     * Get the default NPG api key
     *
     * @return the default api key
     */
    public String getDefaultApiKey() {
        return this.defaultApiKey;
    }

    /**
     * Get the api key associated to the input pspId for the given paymentMethod
     *
     * @param paymentMethod the payment method for which api keys will be searched
     *                      for
     * @param pspId         the searched api key psp id
     * @return either the found api key or an NpgApiKeyConfigurationException
     *         exception if no api key can be found
     */
    public Either<NpgApiKeyConfigurationException, String> getApiKeyForPaymentMethod(
                                                                                     NpgClient.PaymentMethod paymentMethod,
                                                                                     String pspId
    ) {
        Either<NpgApiKeyConfigurationException, String> result = Either
                .left(
                        new NpgApiKeyConfigurationException(
                                "Cannot retrieve api key configuration for payment method: [%s]."
                                        .formatted(paymentMethod)
                        )
                );
        NpgPspApiKeysConfig npgPspApiKeysConfig = this.methodsApiKeyMapping.get(paymentMethod);
        if (npgPspApiKeysConfig != null) {
            result = npgPspApiKeysConfig.get(pspId).bimap(
                    ex -> new NpgApiKeyConfigurationException(
                            "Cannot retrieve api key for payment method: [%s]. Cause: %s"
                                    .formatted(paymentMethod, ex.getMessage())
                    ),
                    Function.identity()
            );
        }
        return result;
    }

    /**
     * Alias for
     * {@link NpgApiKeyConfiguration#getApiKeyForPaymentMethod(NpgClient.PaymentMethod, String)}
     *
     * @param paymentMethod the payment method for which api keys will be searched
     *                      for
     * @param pspId         the searched api key psp id
     * @return either the found api key or an NpgApiKeyConfigurationException
     *         exception if no api key can be found
     */
    public Either<NpgApiKeyConfigurationException, String> get(
                                                               NpgClient.PaymentMethod paymentMethod,
                                                               String pspId
    ) {
        return getApiKeyForPaymentMethod(paymentMethod, pspId);
    }

}
