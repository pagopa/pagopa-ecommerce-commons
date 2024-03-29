package it.pagopa.ecommerce.commons.utils;

import it.pagopa.ecommerce.commons.exceptions.UniqueIdGenerationException;
import it.pagopa.ecommerce.commons.redis.templatewrappers.UniqueIdTemplateWrapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

class UniqueIdUtilsTests {
    private final UniqueIdTemplateWrapper uniqueIdTemplateWrapper = mock(UniqueIdTemplateWrapper.class);

    private final UniqueIdUtils uniqueIdUtils = new UniqueIdUtils(uniqueIdTemplateWrapper);

    private static final String PRODUCT_PREFIX = "E";

    @Test
    void shouldGenerateUniqueIdGenerateException() {
        Mockito.when(uniqueIdTemplateWrapper.saveIfAbsent(any(), any())).thenReturn(false);
        StepVerifier.create(uniqueIdUtils.generateUniqueId())
                .expectErrorMatches(e -> e instanceof UniqueIdGenerationException)
                .verify();
        Mockito.verify(uniqueIdTemplateWrapper, Mockito.times(3)).saveIfAbsent(any(), any());
    }

    @Test
    void shouldGenerateUniqueIdWithRetry() {
        Mockito.when(uniqueIdTemplateWrapper.saveIfAbsent(any(), any())).thenReturn(false, false, true);
        StepVerifier.create(uniqueIdUtils.generateUniqueId())
                .expectNextMatches(
                        response -> response.length() == 18 && response.startsWith(PRODUCT_PREFIX)
                )
                .verifyComplete();
        Mockito.verify(uniqueIdTemplateWrapper, Mockito.times(3)).saveIfAbsent(any(), any());
    }

    @Test
    void shouldGenerateUniqueIdNoRetry() {
        Mockito.when(uniqueIdTemplateWrapper.saveIfAbsent(any(), any())).thenReturn(true);
        StepVerifier.create(uniqueIdUtils.generateUniqueId())
                .expectNextMatches(
                        response -> response.length() == 18 && response.startsWith(PRODUCT_PREFIX)
                )
                .verifyComplete();
        Mockito.verify(uniqueIdTemplateWrapper, Mockito.times(1)).saveIfAbsent(any(), any());
    }
}
