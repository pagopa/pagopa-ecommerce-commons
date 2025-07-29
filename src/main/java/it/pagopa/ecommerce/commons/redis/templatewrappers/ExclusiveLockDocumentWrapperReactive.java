package it.pagopa.ecommerce.commons.redis.templatewrappers;

import it.pagopa.ecommerce.commons.repositories.ExclusiveLockDocument;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;

/**
 * Redis template wrapper instance for handling exclusive lock
 */
public class ExclusiveLockDocumentWrapperReactive extends ReactiveRedisTemplateWrapper<ExclusiveLockDocument> {

    /**
     * Constructor
     *
     * @param redisTemplate inner redis template
     * @param keyspace      keyspace associated to this wrapper
     * @param ttl           time to live for keys
     */
    public ExclusiveLockDocumentWrapperReactive(
            ReactiveRedisTemplate<String, ExclusiveLockDocument> redisTemplate,
            String keyspace,
            Duration ttl
    ) {
        super(redisTemplate, keyspace, ttl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getKeyFromEntity(ExclusiveLockDocument value) {
        return value.id();
    }

}
