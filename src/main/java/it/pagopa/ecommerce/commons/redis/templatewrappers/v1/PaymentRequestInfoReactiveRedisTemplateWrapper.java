package it.pagopa.ecommerce.commons.redis.templatewrappers.v1;

import it.pagopa.ecommerce.commons.redis.templatewrappers.ReactiveRedisTemplateWrapper;
import it.pagopa.ecommerce.commons.repositories.v1.PaymentRequestInfo;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;

/**
 * Wrapper for {@link PaymentRequestInfo} redis template
 */
public final class PaymentRequestInfoReactiveRedisTemplateWrapper
        extends ReactiveRedisTemplateWrapper<PaymentRequestInfo> {
    /**
     * Constructor
     *
     * @param reactiveRedisTemplate - the redis template instance to access entity
     *                              store
     * @param keyspace              - the keyspace on which entities will be stored
     * @param ttl                   - the ttl that will be set on each saved
     *                              entities
     */
    public PaymentRequestInfoReactiveRedisTemplateWrapper(
            ReactiveRedisTemplate<String, PaymentRequestInfo> reactiveRedisTemplate,
            String keyspace,
            Duration ttl
    ) {
        super(reactiveRedisTemplate, keyspace, ttl);
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
