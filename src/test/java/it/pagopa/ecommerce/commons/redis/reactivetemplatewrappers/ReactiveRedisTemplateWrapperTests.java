package it.pagopa.ecommerce.commons.redis.reactivetemplatewrappers;

import it.pagopa.ecommerce.commons.redis.reactivetemplatewrappers.v1.ReactivePaymentRequestInfoRedisTemplateWrapper;
import it.pagopa.ecommerce.commons.redis.reactivetemplatewrappers.v1.ReactiveRedisTemplateWrapperBuilder;
import it.pagopa.ecommerce.commons.repositories.v1.PaymentRequestInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

import static it.pagopa.ecommerce.commons.redis.templatewrappers.v1.RedisTemplateWrapperBuilder.buildJackson2RedisSerializer;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ReactiveRedisTemplateWrapperTests {

    private ReactiveRedisConnectionFactory reactiveRedisConnectionFactory = Mockito.mock(ReactiveRedisConnectionFactory.class);

    @Test
    void shouldBuildPaymentRequestWrapperSuccessfully() {
        Duration ttl = Duration.ofMinutes(10);
        ReactivePaymentRequestInfoRedisTemplateWrapper paymentRequestInfoRedisTemplateWrapper = ReactiveRedisTemplateWrapperBuilder
                .buildPaymentRequestInfoRedisTemplateWrapper(reactiveRedisConnectionFactory, ttl);
        assertNotNull(paymentRequestInfoRedisTemplateWrapper);
        ReactiveRedisTemplate<String, PaymentRequestInfo> reactiveRedisTemplate = paymentRequestInfoRedisTemplateWrapper.unwrap();
        assertNotNull(reactiveRedisTemplate);

        RedisSerializationContext<String, PaymentRequestInfo> context =
                paymentRequestInfoRedisTemplateWrapper.unwrap().getSerializationContext();

        Jackson2JsonRedisSerializer<PaymentRequestInfo> serializer = buildJackson2RedisSerializer(PaymentRequestInfo.class);


        assertEquals(StringRedisSerializer.class, context.getKeySerializationPair().getWriter().getClass());
        assertTrue(serializer.canSerialize(PaymentRequestInfo.class));


//        assertEquals(reactiveRedisTemplate.getKeySerializer().getClass(), StringRedisSerializer.class);
//        assertTrue(redisTemplate.getValueSerializer().canSerialize(PaymentRequestInfo.class));
        assertEquals(ttl, paymentRequestInfoRedisTemplateWrapper.getDefaultTTL());
    }

}
