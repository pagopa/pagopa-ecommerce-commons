package it.pagopa.ecommerce.commons.redis.templatewrappers.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import it.pagopa.ecommerce.commons.domain.v1.IdempotencyKey;
import it.pagopa.ecommerce.commons.domain.v1.RptId;
import it.pagopa.ecommerce.commons.redis.converters.v1.JacksonIdempotencyKeyDeserializer;
import it.pagopa.ecommerce.commons.redis.converters.v1.JacksonIdempotencyKeySerializer;
import it.pagopa.ecommerce.commons.redis.converters.v1.JacksonRptIdDeserializer;
import it.pagopa.ecommerce.commons.redis.converters.v1.JacksonRptIdSerializer;
import it.pagopa.ecommerce.commons.repositories.v1.PaymentRequestInfo;
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
     * Private constructor used to hide default public ones
     */
    private RedisTemplateWrapperBuilder() {
        // Utility class, no need to instantiate it
    }

    /**
     * Build {@link PaymentRequestInfoRedisTemplateWrapper} instance using input
     * redis connection factory and configuring custom converters for {@link RptId},
     * {@link IdempotencyKey} and other domain objects
     *
     * @param redisConnectionFactory - the redis connection factory to be used for
     * @param entitiesTTL            - the default TTL to be applied to all saved
     *                               entities if not overridden
     * @return PaymentRequestInfoRedisTemplateWrapper new instance
     */
    public static PaymentRequestInfoRedisTemplateWrapper buildPaymentRequestInfoRedisTemplateWrapper(
                                                                                                     RedisConnectionFactory redisConnectionFactory,
                                                                                                     Duration entitiesTTL
    ) {
        RedisTemplate<String, PaymentRequestInfo> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        Jackson2JsonRedisSerializer<PaymentRequestInfo> jacksonRedisSerializer = buildJackson2RedisSerializer(
                PaymentRequestInfo.class
        );
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(jacksonRedisSerializer);
        redisTemplate.afterPropertiesSet();
        return new PaymentRequestInfoRedisTemplateWrapper(redisTemplate, "keys", entitiesTTL);
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
        rptSerializationModule.addSerializer(RptId.class, new JacksonRptIdSerializer());
        rptSerializationModule.addDeserializer(RptId.class, new JacksonRptIdDeserializer());
        rptSerializationModule.addSerializer(IdempotencyKey.class, new JacksonIdempotencyKeySerializer());
        rptSerializationModule.addDeserializer(IdempotencyKey.class, new JacksonIdempotencyKeyDeserializer());
        objectMapper.registerModule(rptSerializationModule);
        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
        return jackson2JsonRedisSerializer;
    }
}
