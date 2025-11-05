package it.pagopa.ecommerce.commons.redis.reactivetemplatewrappers.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import it.pagopa.ecommerce.commons.domain.v1.IdempotencyKey;
import it.pagopa.ecommerce.commons.domain.v1.RptId;
import it.pagopa.ecommerce.commons.redis.converters.v1.JacksonIdempotencyKeyDeserializer;
import it.pagopa.ecommerce.commons.redis.converters.v1.JacksonIdempotencyKeySerializer;
import it.pagopa.ecommerce.commons.redis.converters.v1.JacksonRptIdDeserializer;
import it.pagopa.ecommerce.commons.redis.converters.v1.JacksonRptIdSerializer;
import it.pagopa.ecommerce.commons.redis.templatewrappers.v1.PaymentRequestInfoRedisTemplateWrapper;
import it.pagopa.ecommerce.commons.repositories.v1.PaymentRequestInfo;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * Helper class that can be used to properly configure RedisTemplate wrapper
 * adding custom serializers
 */

public class ReactiveRedisTemplateWrapperBuilder {

    /**
     * Private constructor used to hide default public ones
     */
    private ReactiveRedisTemplateWrapperBuilder() {
        // Utility class, no need to instantiate it
    }

    /**
     * Build {@link ReactivePaymentRequestInfoRedisTemplateWrapper} instance using
     * input redis connection factory and configuring custom converters for
     * {@link RptId}, {@link IdempotencyKey} and other domain objects
     *
     * @param reactiveRedisConnectionFactory - the redis connection factory to be
     *                                       used for
     * @param entitiesTTL                    - the default TTL to be applied to all
     *                                       saved entities if not overridden
     * @return ReactivePaymentRequestInfoRedisTemplateWrapper new instance
     */
    public static ReactivePaymentRequestInfoRedisTemplateWrapper buildPaymentRequestInfoRedisTemplateWrapper(
                                                                                                             ReactiveRedisConnectionFactory reactiveRedisConnectionFactory,
                                                                                                             Duration entitiesTTL
    ) {
        Jackson2JsonRedisSerializer<PaymentRequestInfo> serializer = buildJackson2RedisSerializer(
                PaymentRequestInfo.class
        );
        RedisSerializationContext<String, PaymentRequestInfo> serializationContext = RedisSerializationContext
                .<String, PaymentRequestInfo>newSerializationContext(new StringRedisSerializer())
                .value(serializer)
                .build();

        ReactiveRedisTemplate<String, PaymentRequestInfo> reactiveRedisTemplate = new ReactiveRedisTemplate<>(
                reactiveRedisConnectionFactory,
                serializationContext
        );

        return new ReactivePaymentRequestInfoRedisTemplateWrapper(reactiveRedisTemplate, "keys", entitiesTTL);

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
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule rptSerializationModule = new SimpleModule();
        rptSerializationModule.addSerializer(RptId.class, new JacksonRptIdSerializer());
        rptSerializationModule.addDeserializer(RptId.class, new JacksonRptIdDeserializer());
        rptSerializationModule.addSerializer(IdempotencyKey.class, new JacksonIdempotencyKeySerializer());
        rptSerializationModule.addDeserializer(IdempotencyKey.class, new JacksonIdempotencyKeyDeserializer());
        objectMapper.registerModule(rptSerializationModule);
        objectMapper.setSerializationInclusion(NON_NULL);
        return new Jackson2JsonRedisSerializer<>(objectMapper, clazz);
    }
}
