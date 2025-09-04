package it.pagopa.ecommerce.commons.utils;

import it.pagopa.ecommerce.commons.exceptions.UniqueIdGenerationException;
import it.pagopa.ecommerce.commons.redis.reactivetemplatewrappers.ReactiveUniqueIdTemplateWrapper;
import it.pagopa.ecommerce.commons.repositories.UniqueIdDocument;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReactiveUniqueIdUtilsTests {
    private final ReactiveUniqueIdTemplateWrapper reactiveUniqueIdTemplateWrapper = mock(
            ReactiveUniqueIdTemplateWrapper.class
    );

    private final ReactiveUniqueIdUtils reactiveUniqueIdUtils = new ReactiveUniqueIdUtils(
            reactiveUniqueIdTemplateWrapper
    );

    private static final String PRODUCT_PREFIX = "E";

    @Test
    void shouldGenerateUniqueIdGenerateException() {
        when(reactiveUniqueIdTemplateWrapper.saveIfAbsent(any(), any())).thenReturn(Mono.just(false));
        StepVerifier.create(reactiveUniqueIdUtils.generateUniqueId())
                .expectErrorMatches(e -> e instanceof UniqueIdGenerationException)
                .verify();
        verify(reactiveUniqueIdTemplateWrapper, Mockito.times(3)).saveIfAbsent(any(), any());
    }

    @Test
    void shouldGenerateUniqueIdWithRetry() {
        when(reactiveUniqueIdTemplateWrapper.saveIfAbsent(any(), any()))
                .thenReturn(Mono.just(false), Mono.just(false), Mono.just(true));
        StepVerifier.create(reactiveUniqueIdUtils.generateUniqueId())
                .expectNextMatches(
                        response -> response.length() == 18 && response.startsWith(PRODUCT_PREFIX)
                )
                .verifyComplete();
        verify(reactiveUniqueIdTemplateWrapper, Mockito.times(3)).saveIfAbsent(any(), any());
    }

    @Test
    void shouldGenerateUniqueIdNoRetry() {
        when(reactiveUniqueIdTemplateWrapper.saveIfAbsent(any(), any())).thenReturn(Mono.just(true));
        StepVerifier.create(reactiveUniqueIdUtils.generateUniqueId())
                .expectNextMatches(
                        response -> response.length() == 18 && response.startsWith(PRODUCT_PREFIX)
                )
                .verifyComplete();
        verify(reactiveUniqueIdTemplateWrapper, Mockito.times(1)).saveIfAbsent(any(), any());
    }

    @Test
    void shouldThrowUniqueIdGenerateExceptionOnExhaustedAttemptsGeneratingId() {
        ArgumentCaptor<UniqueIdDocument> uniqueIdSaveArgumentCaptor =
                ArgumentCaptor.forClass(UniqueIdDocument.class);

        when(reactiveUniqueIdTemplateWrapper.saveIfAbsent(uniqueIdSaveArgumentCaptor.capture(), any(Duration.class)))
                .thenReturn(Mono.just(false));

        StepVerifier.create(reactiveUniqueIdUtils.generateUniqueId())
                .expectError(UniqueIdGenerationException.class)
                .verify();

        verify(reactiveUniqueIdTemplateWrapper, times(3))
                .saveIfAbsent(any(UniqueIdDocument.class), any(Duration.class));

        List<UniqueIdDocument> allDocs = uniqueIdSaveArgumentCaptor.getAllValues();
        Set<String> savedIds = allDocs.stream()
                .map(doc -> {
                    try { return doc.id(); } catch (Throwable ignore) { return doc.id(); }
                })
                .collect(Collectors.toSet());

        assertThat(savedIds).hasSize(allDocs.size());
    }




}
