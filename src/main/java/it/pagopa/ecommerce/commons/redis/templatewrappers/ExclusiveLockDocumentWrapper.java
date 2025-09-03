package it.pagopa.ecommerce.commons.redis.templatewrappers;

import it.pagopa.ecommerce.commons.repositories.ExclusiveLockDocument;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;

/**
 * Redis template wrapper instance for handling exclusive lock.
 *
 * @deprecated Use
 *             {@link it.pagopa.ecommerce.commons.redis.reactivetemplatewrappers.ReactiveExclusiveLockDocumentWrapper}
 *             instead.
 */
public class ExclusiveLockDocumentWrapper extends RedisTemplateWrapper<ExclusiveLockDocument> {

    /**
     * Constructor
     *
     * @param redisTemplate inner redis template
     * @param keyspace      keyspace associated to this wrapper
     * @param ttl           time to live for keys
     */
    public ExclusiveLockDocumentWrapper(
            RedisTemplate<String, ExclusiveLockDocument> redisTemplate,
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
