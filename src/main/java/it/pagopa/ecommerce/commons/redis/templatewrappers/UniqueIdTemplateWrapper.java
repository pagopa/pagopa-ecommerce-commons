package it.pagopa.ecommerce.commons.redis.templatewrappers;

import it.pagopa.ecommerce.commons.repositories.UniqueIdDocument;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;

/**
 * Wrapper for {@link UniqueIdDocument} redis template
 */
public class UniqueIdTemplateWrapper extends RedisTemplateWrapper<UniqueIdDocument> {
    /**
     * Primary constructor
     *
     * @param redisTemplate inner redis template
     * @param keyspace      keyspace associated to this wrapper
     * @param ttl           time to live for keys
     */
    public UniqueIdTemplateWrapper(
            RedisTemplate<String, UniqueIdDocument> redisTemplate,
            String keyspace,
            Duration ttl
    ) {
        super(redisTemplate, keyspace, ttl);
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
