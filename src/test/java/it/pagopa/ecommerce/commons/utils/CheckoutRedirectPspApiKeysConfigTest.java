package it.pagopa.ecommerce.commons.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.trace.Span;
import io.vavr.control.Either;
import it.pagopa.ecommerce.commons.exceptions.CheckoutRedirectConfigurationException;
import it.pagopa.ecommerce.commons.exceptions.CheckoutRedirectMissingPspRequestedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class CheckoutRedirectPspApiKeysConfigTest {
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
    void shouldParsePspConfigurationSuccessfully(String pspId) throws CheckoutRedirectMissingPspRequestedException {
        Either<CheckoutRedirectConfigurationException, CheckoutRedirectPspApiKeysConfig> pspConfiguration = CheckoutRedirectPspApiKeysConfig
                .parseApiKeyConfiguration(
                        pspConfigurationJson,
                        pspToHandle,
                        OBJECT_MAPPER
                );

        assertTrue(pspConfiguration.isRight());
        assertEquals("key-%s".formatted(pspId), pspConfiguration.get().get(pspId).get());
    }

    @Test
    void shouldThrowExceptionForInvalidJsonStructure() {
        Either<CheckoutRedirectConfigurationException, CheckoutRedirectPspApiKeysConfig> pspConfiguration = CheckoutRedirectPspApiKeysConfig
                .parseApiKeyConfiguration(
                        "{",
                        pspToHandle,
                        OBJECT_MAPPER
                );
        assertTrue(pspConfiguration.isLeft());
        assertEquals(
                "Error parsing Checkout Redirect PSP api keys configuration, cause: Invalid json configuration map",
                pspConfiguration.getLeft().getMessage()
        );
    }

    @Test
    void shouldThrowExceptionForMissingPspId() {
        Set<String> psps = new HashSet<>(pspToHandle);
        psps.add("psp4");
        Either<CheckoutRedirectConfigurationException, CheckoutRedirectPspApiKeysConfig> pspConfiguration = CheckoutRedirectPspApiKeysConfig
                .parseApiKeyConfiguration(
                        pspConfigurationJson,
                        psps,
                        OBJECT_MAPPER
                );
        assertTrue(pspConfiguration.isLeft());
        assertEquals(
                "Error parsing Checkout Redirect PSP api keys configuration, cause: Misconfigured api keys. Missing keys: [psp4]",
                pspConfiguration.getLeft().getMessage()
        );
    }

    @Test
    void shouldThrowExceptionForRetrievingMissingPsp() {
        Either<CheckoutRedirectConfigurationException, CheckoutRedirectPspApiKeysConfig> pspConfiguration = CheckoutRedirectPspApiKeysConfig
                .parseApiKeyConfiguration(
                        pspConfigurationJson,
                        pspToHandle,
                        OBJECT_MAPPER
                );

        Span invalidSpan = Span.getInvalid();
        try (MockedStatic<Span> s = Mockito.mockStatic(Span.class)) {
            s.when(Span::current).thenReturn(invalidSpan);

            assertTrue(pspConfiguration.isRight());
            assertInstanceOf(
                    CheckoutRedirectMissingPspRequestedException.class,
                    pspConfiguration.get().get("missingPSP").getLeft()
            );
        }
    }
}
