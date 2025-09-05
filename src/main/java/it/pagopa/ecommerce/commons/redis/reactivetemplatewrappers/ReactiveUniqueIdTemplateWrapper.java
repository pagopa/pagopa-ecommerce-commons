package it.pagopa.ecommerce.commons.redis.reactivetemplatewrappers;

import it.pagopa.ecommerce.commons.repositories.UniqueIdDocument;
import org.springframework.data.redis.core.ReactiveRedisTemplate;

import java.time.Duration;

/**
 * Wrapper for {@link UniqueIdDocument} redis template
 */
public class ReactiveUniqueIdTemplateWrapper extends ReactiveRedisTemplateWrapper<UniqueIdDocument> {
    /**
     * Primary constructor
     *
     * @param reactiveRedisTemplate inner redis template
     * @param keyspace              keyspace associated to this wrapper
     * @param ttl                   time to live for keys
     */
    public ReactiveUniqueIdTemplateWrapper(
            ReactiveRedisTemplate<String, UniqueIdDocument> reactiveRedisTemplate,
            String keyspace,
            Duration ttl
    ) {
        super(reactiveRedisTemplate, keyspace, ttl);
    }

    /**
     * Extract key from input {@link UniqueIdDocument}
     *
     * @param value - the entity to be saved
     * @return the entity id string representation
     */
    @Override
    protected String getKeyFromEntity(UniqueIdDocument value) {
        return value.id();
    }
}
