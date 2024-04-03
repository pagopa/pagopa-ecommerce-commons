package it.pagopa.ecommerce.commons.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Either;
import it.pagopa.ecommerce.commons.client.NpgClient;
import it.pagopa.ecommerce.commons.exceptions.NpgApiKeyConfigurationException;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NpgApiKeyConfigurationTest {

    private static final String DEFAULT_API_KEY = "default-api-key";

    private static final String PSP_ID = "pspId1";
    private final NpgApiKeyConfiguration npgApiKeyConfiguration = new NpgApiKeyConfiguration.Builder()
            .setDefaultApiKey(DEFAULT_API_KEY)
            .withMethodPspMapping(
                    NpgClient.PaymentMethod.PAYPAL,
                    new NpgPspApiKeysConfig(
                            Map.of(PSP_ID, "pspId1-paypal-api-key")
                    )
            ).withMethodPspMapping(
                    NpgClient.PaymentMethod.CARDS,
                    new NpgPspApiKeysConfig(
                            Map.of(PSP_ID, "pspId1-cards-api-key")
                    )
            )
            .build();

    @Test
    void shouldRetrieveApiKeySuccessfully() {
        // test
        String defaultApiKey = npgApiKeyConfiguration.getDefaultApiKey();
        Either<NpgApiKeyConfigurationException, String> paypalApiKey = npgApiKeyConfiguration
                .getApiKeyForPaymentMethod(NpgClient.PaymentMethod.PAYPAL, PSP_ID);
        Either<NpgApiKeyConfigurationException, String> cardsApiKey = npgApiKeyConfiguration
                .getApiKeyForPaymentMethod(NpgClient.PaymentMethod.CARDS, PSP_ID);
        // assertions
        assertEquals(DEFAULT_API_KEY, defaultApiKey);
        assertEquals("pspId1-paypal-api-key", paypalApiKey.get());
        assertEquals("pspId1-cards-api-key", cardsApiKey.get());
    }

    @Test
    void shouldThrowExceptionAddingAlreadyExistingApiMapping() {
        // test
        NpgApiKeyConfigurationException exception = assertThrows(
                NpgApiKeyConfigurationException.class,
                () -> new NpgApiKeyConfiguration.Builder()
                        .setDefaultApiKey(DEFAULT_API_KEY)
                        .withMethodPspMapping(
                                NpgClient.PaymentMethod.PAYPAL,
                                new NpgPspApiKeysConfig(
                                        Map.of(PSP_ID, "pspId1-paypal-api-key")
                                )
                        ).withMethodPspMapping(
                                NpgClient.PaymentMethod.PAYPAL,
                                new NpgPspApiKeysConfig(
                                        Map.of(PSP_ID, "pspId1-paypal-api-key")
                                )
                        )
                        .build()
        );
        assertEquals("Api key mapping already registered for payment method: [PAYPAL]", exception.getMessage());

    }

    @Test
    void shouldAddPaymentMethodMappingParsingConfiguration() {
        // test
        NpgApiKeyConfiguration npgApiKeyConfiguration = new NpgApiKeyConfiguration.Builder()
                .setDefaultApiKey(DEFAULT_API_KEY)
                .withMethodPspMapping(
                        NpgClient.PaymentMethod.BANCOMATPAY,
                        """
                                {
                                    "%s": "pspId1-bancomatpay-api-key"
                                }
                                """.formatted(PSP_ID),
                        Set.of(PSP_ID),
                        new ObjectMapper()
                )
                .build();
        Either<NpgApiKeyConfigurationException, String> apiKey = npgApiKeyConfiguration
                .getApiKeyForPaymentMethod(NpgClient.PaymentMethod.BANCOMATPAY, PSP_ID);
        // assertions
        assertEquals("pspId1-bancomatpay-api-key", apiKey.get());
    }

    @Test
    void shouldThrowErrorAddingPaymentMethodMappingParsingWrongConfiguration() {
        // test
        NpgApiKeyConfigurationException exception = assertThrows(
                NpgApiKeyConfigurationException.class,
                () -> new NpgApiKeyConfiguration.Builder()
                        .setDefaultApiKey(DEFAULT_API_KEY)
                        .withMethodPspMapping(
                                NpgClient.PaymentMethod.BANCOMATPAY,
                                """
                                        {

                                        }
                                        """,
                                Set.of(PSP_ID),
                                new ObjectMapper()
                        )
                        .build()
        );

        // assertions
        assertEquals(
                "Error parsing NPG PSP api keys configuration for payment method: [BANCOMATPAY], cause: Misconfigured api keys. Missing keys: [pspId1]",
                exception.getMessage()
        );
    }

    @Test
    void shouldReturnEitherLeftForMissingApiKey() {
        // test
        String pspId = "missingPspId";
        Either<NpgApiKeyConfigurationException, String> paypalApiKey = npgApiKeyConfiguration
                .getApiKeyForPaymentMethod(NpgClient.PaymentMethod.PAYPAL, pspId);
        // assertions
        assertEquals(
                "Cannot retrieve api key for payment method: [PAYPAL]. Cause: Requested API key for PSP: [missingPspId]. Available PSPs: [pspId1]",
                paypalApiKey.getLeft().getMessage()
        );
    }

    @Test
    void shouldThrowExceptionForMissingDefaultApiKey() {
        // test
        NpgApiKeyConfigurationException exception = assertThrows(
                NpgApiKeyConfigurationException.class,
                () -> new NpgApiKeyConfiguration.Builder()
                        .withMethodPspMapping(
                                NpgClient.PaymentMethod.PAYPAL,
                                new NpgPspApiKeysConfig(
                                        Map.of(PSP_ID, "pspId1-paypal-api-key")
                                )
                        ).withMethodPspMapping(
                                NpgClient.PaymentMethod.CARDS,
                                new NpgPspApiKeysConfig(
                                        Map.of(PSP_ID, "pspId1-cards-api-key")
                                )
                        )
                        .build()
        );
        // assertions
        assertEquals("Invalid configuration detected! Default api key mapping cannot be null", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionForMissingMethodsKeys() {
        // test
        NpgApiKeyConfigurationException exception = assertThrows(
                NpgApiKeyConfigurationException.class,
                () -> new NpgApiKeyConfiguration.Builder()
                        .setDefaultApiKey(DEFAULT_API_KEY)
                        .build()
        );
        // assertions
        assertEquals(
                "Invalid configuration detected! Payment methods api key mapping cannot be null or empty",
                exception.getMessage()
        );
    }

}
