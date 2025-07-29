package it.pagopa.ecommerce.commons.redis.templatewrappers;

import it.pagopa.ecommerce.commons.repositories.ExclusiveLockDocument;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExclusiveLockDocumentWrapperTest {

    private final ReactiveRedisTemplate<String, ExclusiveLockDocument> reactiveRedisTemplate = Mockito
            .mock(ReactiveRedisTemplate.class);
    private final ExclusiveLockDocumentWrapperReactive exclusiveLockDocumentWrapper = new ExclusiveLockDocumentWrapperReactive(
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
