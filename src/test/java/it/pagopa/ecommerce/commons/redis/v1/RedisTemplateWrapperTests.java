package it.pagopa.ecommerce.commons.redis.v1;

import it.pagopa.ecommerce.commons.redis.templatewrappers.v1.PaymentRequestInfoRedisTemplateWrapper;
import it.pagopa.ecommerce.commons.redis.templatewrappers.v1.RedisTemplateWrapperBuilder;
import it.pagopa.ecommerce.commons.repositories.v1.PaymentRequestInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RedisTemplateWrapperTests {

    private RedisConnectionFactory redisConnectionFactory = Mockito.mock(RedisConnectionFactory.class);

    @Test
    void shouldBuildPaymentRequestWrapperSuccessfully() {
        Duration ttl = Duration.ofMinutes(10);
        PaymentRequestInfoRedisTemplateWrapper paymentRequestInfoRedisTemplateWrapper = RedisTemplateWrapperBuilder
                .buildPaymentRequestInfoRedisTemplateWrapper(redisConnectionFactory, ttl);
        assertNotNull(paymentRequestInfoRedisTemplateWrapper);
        RedisTemplate<String, PaymentRequestInfo> redisTemplate = paymentRequestInfoRedisTemplateWrapper.unwrap();
        assertNotNull(redisTemplate);
        assertEquals(redisTemplate.getKeySerializer().getClass(), StringRedisSerializer.class);
        assertTrue(redisTemplate.getValueSerializer().canSerialize(PaymentRequestInfo.class));
        assertEquals(ttl, paymentRequestInfoRedisTemplateWrapper.getDefaultTTL());
    }

}
