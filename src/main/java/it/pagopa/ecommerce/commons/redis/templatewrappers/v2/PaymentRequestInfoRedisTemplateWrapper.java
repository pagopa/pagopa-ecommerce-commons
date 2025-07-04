package it.pagopa.ecommerce.commons.redis.templatewrappers.v2;

import it.pagopa.ecommerce.commons.redis.templatewrappers.RedisTemplateWrapper;
import it.pagopa.ecommerce.commons.repositories.v2.PaymentRequestInfo;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;

/**
 * Wrapper for {@link PaymentRequestInfo} redis template
 */
public final class PaymentRequestInfoRedisTemplateWrapper extends RedisTemplateWrapper<PaymentRequestInfo> {
    /**
     * Constructor
     *
     * @param redisTemplate - the redis template instance to access entity store
     * @param keyspace      - the keyspace on which entities will be stored
     * @param ttl           - the ttl that will be set on each saved entities
     */
    public PaymentRequestInfoRedisTemplateWrapper(
            RedisTemplate<String, PaymentRequestInfo> redisTemplate,
            String keyspace,
            Duration ttl
    ) {
        super(redisTemplate, keyspace, ttl);
    }

    /**
     * Extract key from input {@link PaymentRequestInfo}
     *
     * @param value - the entity to be saved
     * @return the entity rpt id string representation
     */
    @Override
    protected String getKeyFromEntity(PaymentRequestInfo value) {
        return value.id().value();
    }
}
