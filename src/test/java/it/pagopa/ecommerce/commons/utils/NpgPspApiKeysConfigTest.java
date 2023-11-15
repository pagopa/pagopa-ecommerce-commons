package it.pagopa.ecommerce.commons.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.trace.Span;
import io.vavr.control.Either;
import it.pagopa.ecommerce.commons.client.NpgClient;
import it.pagopa.ecommerce.commons.exceptions.NpgApiKeyConfigurationException;
import it.pagopa.ecommerce.commons.exceptions.NpgApiKeyMissingPspRequested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class NpgPspApiKeysConfigTest {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final String pspConfigurationJson = """
            {
                "psp1" : "key-psp1",
                "psp2" : "key-psp2",
                "psp3" : "key-psp3"
            }
            """;

    private final Set<String> pspToHandle = Set.of("psp1", "psp2", "psp3");

    @ParameterizedTest
    @ValueSource(
            strings = {
                    "psp1",
                    "psp2",
                    "psp3"
            }
    )
    void shouldParsePspConfigurationSuccessfully(String pspId) throws NpgApiKeyMissingPspRequested {
        Either<NpgApiKeyConfigurationException, NpgPspApiKeysConfig> pspConfiguration = NpgPspApiKeysConfig
                .parseApiKeyConfiguration(
                        pspConfigurationJson,
                        pspToHandle,
                        NpgClient.PaymentMethod.CARDS,
                        OBJECT_MAPPER
                );

        assertTrue(pspConfiguration.isRight());
        assertDoesNotThrow(() -> assertEquals("key-%s".formatted(pspId), pspConfiguration.get().get(pspId)));
    }

    @Test
    void shouldThrowExceptionForInvalidJsonStructure() {
        Either<NpgApiKeyConfigurationException, NpgPspApiKeysConfig> pspConfiguration = NpgPspApiKeysConfig
                .parseApiKeyConfiguration(
                        "{",
                        pspToHandle,
                        NpgClient.PaymentMethod.CARDS,
                        OBJECT_MAPPER
                );
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
        Either<NpgApiKeyConfigurationException, NpgPspApiKeysConfig> pspConfiguration = NpgPspApiKeysConfig
                .parseApiKeyConfiguration(
                        pspConfigurationJson,
                        psps,
                        NpgClient.PaymentMethod.CARDS,
                        OBJECT_MAPPER
                );
        assertTrue(pspConfiguration.isLeft());
        assertEquals(
                "Error parsing NPG PSP api keys configuration for payment method: [CARDS], cause: Misconfigured api keys. Missing keys: [psp4]",
                pspConfiguration.getLeft().getMessage()
        );
    }

    @Test
    void shouldThrowExceptionForRetrievingMissingPsp() {
        Either<NpgApiKeyConfigurationException, NpgPspApiKeysConfig> pspConfiguration = NpgPspApiKeysConfig
                .parseApiKeyConfiguration(
                        pspConfigurationJson,
                        pspToHandle,
                        NpgClient.PaymentMethod.CARDS,
                        OBJECT_MAPPER
                );

        Span invalidSpan = Span.getInvalid();
        try (MockedStatic<Span> s = Mockito.mockStatic(Span.class)) {
            s.when(Span::current).thenReturn(invalidSpan);

            assertTrue(pspConfiguration.isRight());
            assertThrows(NpgApiKeyMissingPspRequested.class, () -> pspConfiguration.get().get("missingPSP"));
        }
    }
}
