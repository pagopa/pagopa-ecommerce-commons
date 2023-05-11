package it.pagopa.ecommerce.commons.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import it.pagopa.ecommerce.commons.domain.v1.IdempotencyKey;
import it.pagopa.ecommerce.commons.domain.v1.RptId;
import it.pagopa.ecommerce.commons.redis.converters.JacksonIdempotencyKeyDeserializer;
import it.pagopa.ecommerce.commons.redis.converters.JacksonIdempotencyKeySerializer;
import it.pagopa.ecommerce.commons.redis.converters.JacksonRptDeserializer;
import it.pagopa.ecommerce.commons.redis.converters.JacksonRptSerializer;
import it.pagopa.ecommerce.commons.redis.templatewrappers.PaymentRequestInfoRedisTemplateWrapper;
import it.pagopa.ecommerce.commons.repositories.PaymentRequestInfo;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Helper class that can be used to properly configure RedisTemplate wrapper
 * adding custom serializers
 */

public class RedisTemplateWrapperBuilder {

    /**
     * Build {@link PaymentRequestInfoRedisTemplateWrapper} instance using input
     * redis connection factory and configuring custom converters for {@link RptId},
     * {@link IdempotencyKey} and other domain objects
     *
     * @param redisConnectionFactory - the redis connection factory to be used for
     *                               RedisTemplate
     * @return PaymentRequestInfoRedisTemplateWrapper new instance
     */
    public static PaymentRequestInfoRedisTemplateWrapper buildPaymentRequestInfoRedisTemplateWrapper(
                                                                                                     RedisConnectionFactory redisConnectionFactory
    ) {
        RedisTemplate<String, PaymentRequestInfo> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        Jackson2JsonRedisSerializer<PaymentRequestInfo> jacksonRedisSerializer = buildJackson2RedisSerializer(
                PaymentRequestInfo.class
        );
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(jacksonRedisSerializer);
        redisTemplate.afterPropertiesSet();
        return new PaymentRequestInfoRedisTemplateWrapper(redisTemplate, "keys", Duration.ofMinutes(10));
    }

    /**
     * Build {@link Jackson2JsonRedisSerializer} specialized instance with object
     * mapper configured to handle {@link RptId} and {@link IdempotencyKey}
     * serialization/deserialization
     *
     * @param clazz - the entity class for which create
     *              {@link Jackson2JsonRedisSerializer} instance
     * @return Jackson2JsonRedisSerializer configured for handling all domain
     *         objects serialization proper serialization
     */
    private static <T> Jackson2JsonRedisSerializer<T> buildJackson2RedisSerializer(Class<T> clazz) {
        Jackson2JsonRedisSerializer<T> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(clazz);
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule rptSerializationModule = new SimpleModule();
        rptSerializationModule.addSerializer(RptId.class, new JacksonRptSerializer());
        rptSerializationModule.addDeserializer(RptId.class, new JacksonRptDeserializer());
        rptSerializationModule.addSerializer(IdempotencyKey.class, new JacksonIdempotencyKeySerializer());
        rptSerializationModule.addDeserializer(IdempotencyKey.class, new JacksonIdempotencyKeyDeserializer());
        objectMapper.registerModule(rptSerializationModule);
        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
        return jackson2JsonRedisSerializer;
    }
}
