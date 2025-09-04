package it.pagopa.ecommerce.commons.utils;

import it.pagopa.ecommerce.commons.exceptions.UniqueIdGenerationException;
import it.pagopa.ecommerce.commons.redis.reactivetemplatewrappers.ReactiveUniqueIdTemplateWrapper;
import it.pagopa.ecommerce.commons.redis.templatewrappers.UniqueIdTemplateWrapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

class REactiveUniqueIdUtilsTests {
    private final ReactiveUniqueIdTemplateWrapper reactiveUniqueIdTemplateWrapper = mock(
            ReactiveUniqueIdTemplateWrapper.class
    );

    private final ReactiveUniqueIdUtils reactiveUniqueIdUtils = new ReactiveUniqueIdUtils(
            reactiveUniqueIdTemplateWrapper
    );

    private static final String PRODUCT_PREFIX = "E";

    @Test
    void shouldGenerateUniqueIdGenerateException() {
        Mockito.when(reactiveUniqueIdTemplateWrapper.saveIfAbsent(any(), any())).thenReturn(Mono.just(false));
        StepVerifier.create(reactiveUniqueIdUtils.generateUniqueId())
                .expectErrorMatches(e -> e instanceof UniqueIdGenerationException)
                .verify();
        Mockito.verify(reactiveUniqueIdTemplateWrapper, Mockito.times(3)).saveIfAbsent(any(), any());
    }

    @Test
    void shouldGenerateUniqueIdWithRetry() {
        Mockito.when(reactiveUniqueIdTemplateWrapper.saveIfAbsent(any(), any()))
                .thenReturn(Mono.just(false), Mono.just(false), Mono.just(true));
        StepVerifier.create(reactiveUniqueIdUtils.generateUniqueId())
                .expectNextMatches(
                        response -> response.length() == 18 && response.startsWith(PRODUCT_PREFIX)
                )
                .verifyComplete();
        Mockito.verify(reactiveUniqueIdTemplateWrapper, Mockito.times(3)).saveIfAbsent(any(), any());
    }

    @Test
    void shouldGenerateUniqueIdNoRetry() {
        Mockito.when(reactiveUniqueIdTemplateWrapper.saveIfAbsent(any(), any())).thenReturn(Mono.just(true));
        StepVerifier.create(reactiveUniqueIdUtils.generateUniqueId())
                .expectNextMatches(
                        response -> response.length() == 18 && response.startsWith(PRODUCT_PREFIX)
                )
                .verifyComplete();
        Mockito.verify(reactiveUniqueIdTemplateWrapper, Mockito.times(1)).saveIfAbsent(any(), any());
    }
}
