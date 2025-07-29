package it.pagopa.ecommerce.commons.redis.v2;

import it.pagopa.ecommerce.commons.redis.templatewrappers.v1.PaymentRequestInfoReactiveRedisTemplateWrapper;
import it.pagopa.ecommerce.commons.redis.templatewrappers.v1.RedisTemplateWrapperBuilder;
import it.pagopa.ecommerce.commons.redis.templatewrappers.v2.PaymentRequestInfoRedisTemplateWrapper;
import it.pagopa.ecommerce.commons.repositories.v1.PaymentRequestInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.nio.ByteBuffer;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RedisTemplateWrapperTests {

    private ReactiveRedisConnectionFactory redisConnectionFactory = Mockito.mock(ReactiveRedisConnectionFactory.class);

    @Test
    void shouldBuildPaymentRequestWrapperSuccessfully() {
        Duration ttl = Duration.ofMinutes(10);

        PaymentRequestInfoReactiveRedisTemplateWrapper paymentRequestInfoRedisTemplateWrapper =
                RedisTemplateWrapperBuilder.buildPaymentRequestInfoRedisTemplateWrapper(redisConnectionFactory, ttl);

        assertNotNull(paymentRequestInfoRedisTemplateWrapper);

        ReactiveRedisTemplate<String, PaymentRequestInfo> redisTemplate = paymentRequestInfoRedisTemplateWrapper.unwrap();
        assertNotNull(redisTemplate);

        RedisSerializationContext<String, PaymentRequestInfo> serializationContext = redisTemplate.getSerializationContext();
        RedisSerializationContext.SerializationPair<PaymentRequestInfo> valueSerializationPair =
                serializationContext.getValueSerializationPair();

        PaymentRequestInfo value = Mockito.mock(PaymentRequestInfo.class);

        assertDoesNotThrow(() -> {
            ByteBuffer buffer = valueSerializationPair.write(value);
            assertNotNull(buffer);
        });

        assertEquals(ttl, paymentRequestInfoRedisTemplateWrapper.getDefaultTTL());
    }

}
