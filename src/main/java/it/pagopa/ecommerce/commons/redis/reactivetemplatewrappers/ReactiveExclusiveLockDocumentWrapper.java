package it.pagopa.ecommerce.commons.redis.reactivetemplatewrappers;

import it.pagopa.ecommerce.commons.repositories.ExclusiveLockDocument;
import org.springframework.data.redis.core.ReactiveRedisTemplate;

import java.time.Duration;

/**
 * Redis template wrapper instance for handling exclusive lock
 */
public class ReactiveExclusiveLockDocumentWrapper extends ReactiveRedisTemplateWrapper<ExclusiveLockDocument> {

    /**
     * Constructor
     *
     * @param reactiveRedisTemplate inner redis template
     * @param keyspace              keyspace associated to this wrapper
     * @param ttl                   time to live for keys
     */
    public ReactiveExclusiveLockDocumentWrapper(
            ReactiveRedisTemplate<String, ExclusiveLockDocument> reactiveRedisTemplate,
            String keyspace,
            Duration ttl
    ) {
        super(reactiveRedisTemplate, keyspace, ttl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getKeyFromEntity(ExclusiveLockDocument value) {
        return value.id();
    }

}
