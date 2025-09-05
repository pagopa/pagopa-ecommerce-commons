package it.pagopa.ecommerce.commons.redis.reactivetemplatewrappers;

import it.pagopa.ecommerce.commons.repositories.ExclusiveLockDocument;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.ReactiveRedisTemplate;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReactiveExclusiveLockDocumentWrapperTest {

    private final ReactiveRedisTemplate<String, ExclusiveLockDocument> reactiveRedisTemplate = Mockito
            .mock(ReactiveRedisTemplate.class);
    private final ReactiveExclusiveLockDocumentWrapper exclusiveLockDocumentWrapper = new ReactiveExclusiveLockDocumentWrapper(
            reactiveRedisTemplate,
            "keyspace",
            Duration.ofSeconds(1)
    );

    @Test
    void shouldExtractKeySuccessfully() {
        String expectedDocumentKey = "expectedKey";
        ExclusiveLockDocument exclusiveLockDocument = new ExclusiveLockDocument(
                expectedDocumentKey,
                "holderName"
        );
        String extractedKey = exclusiveLockDocumentWrapper.getKeyFromEntity(exclusiveLockDocument);
        assertEquals(expectedDocumentKey, extractedKey);
    }

}
