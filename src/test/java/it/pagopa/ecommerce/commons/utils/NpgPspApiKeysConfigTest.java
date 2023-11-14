package it.pagopa.ecommerce.commons.utils;

import io.vavr.control.Either;
import it.pagopa.ecommerce.commons.client.NpgClient;
import it.pagopa.ecommerce.commons.exceptions.NpgApiKeyConfigurationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NpgPspApiKeysConfigTest {

    private final String pspConfigurationJson = """
            {
                "psp1" : "key-psp1",
                "psp2" : "key-psp2",
                "psp3" : "key-psp3"
            }
            """;

    private final Set<String> pspToHandle = Set.of("psp1", "psp2", "psp3");

    private final NpgPspApiKeysConfig npgPspApiKeysConfig = new NpgPspApiKeysConfig(
            pspConfigurationJson,
            pspToHandle,
            NpgClient.PaymentMethod.CARDS
    );

    @ParameterizedTest
    @ValueSource(
            strings = {
                    "psp1",
                    "psp2",
                    "psp3"
            }
    )
    void shouldParsePspConfigurationSuccessfully(String pspId) {
        Either<NpgApiKeyConfigurationException, Map<String, String>> pspConfiguration = npgPspApiKeysConfig
                .parseApiKeyConfiguration();
        assertTrue(pspConfiguration.isRight());
        assertEquals("key-%s".formatted(pspId), pspConfiguration.get().get(pspId));
    }

    @Test
    void shouldThrowExceptionForInvalidJsonStructure() {
        NpgPspApiKeysConfig npgPspApiKeysConfig = new NpgPspApiKeysConfig(
                "{",
                pspToHandle,
                NpgClient.PaymentMethod.CARDS
        );
        Either<NpgApiKeyConfigurationException, Map<String, String>> pspConfiguration = npgPspApiKeysConfig
                .parseApiKeyConfiguration();
        assertTrue(pspConfiguration.isLeft());
        assertEquals(
                "Error parsing NPG PSP api keys configuration for payment method: [CARDS], cause: Invalid json configuration map",
                pspConfiguration.getLeft().getMessage()
        );
    }

    @Test
    void shouldThrowExceptionForMissingPspId() {
        Set<String> psps = new HashSet<>(pspToHandle);
        psps.add("psp4");
        NpgPspApiKeysConfig npgPspApiKeysConfig = new NpgPspApiKeysConfig(
                pspConfigurationJson,
                psps,
                NpgClient.PaymentMethod.CARDS
        );
        Either<NpgApiKeyConfigurationException, Map<String, String>> pspConfiguration = npgPspApiKeysConfig
                .parseApiKeyConfiguration();
        assertTrue(pspConfiguration.isLeft());
        assertEquals(
                "Error parsing NPG PSP api keys configuration for payment method: [CARDS], cause: Misconfigured api keys. Missing keys: [psp4]",
                pspConfiguration.getLeft().getMessage()
        );
    }
}
