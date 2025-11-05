package it.pagopa.ecommerce.commons.redis.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import it.pagopa.ecommerce.commons.domain.v1.IdempotencyKey;
import it.pagopa.ecommerce.commons.domain.v1.RptId;
import it.pagopa.ecommerce.commons.redis.converters.v1.JacksonIdempotencyKeyDeserializer;
import it.pagopa.ecommerce.commons.redis.converters.v1.JacksonIdempotencyKeySerializer;
import it.pagopa.ecommerce.commons.redis.converters.v1.JacksonRptIdDeserializer;
import it.pagopa.ecommerce.commons.redis.converters.v1.JacksonRptIdSerializer;
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

import java.nio.ByteBuffer;
import java.time.Duration;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ReactiveRedisTemplateWrapperTests {

    private ReactiveRedisConnectionFactory reactiveRedisConnectionFactory = Mockito
            .mock(ReactiveRedisConnectionFactory.class);

    @Test
    void shouldBuildPaymentRequestWrapperSuccessfully() {
        Duration ttl = Duration.ofMinutes(10);
        ReactivePaymentRequestInfoRedisTemplateWrapper paymentRequestInfoRedisTemplateWrapper = ReactiveRedisTemplateWrapperBuilder
                .buildPaymentRequestInfoRedisTemplateWrapper(reactiveRedisConnectionFactory, ttl);
        assertNotNull(paymentRequestInfoRedisTemplateWrapper);
        ReactiveRedisTemplate<String, PaymentRequestInfo> reactiveRedisTemplate = paymentRequestInfoRedisTemplateWrapper
                .unwrap();
        assertNotNull(reactiveRedisTemplate);
        assertEquals(ttl, paymentRequestInfoRedisTemplateWrapper.getDefaultTTL());
    }

    @Test
    void shouldWriteBuildPaymentRequestWrapperSuccessfully() {
        Duration ttl = Duration.ofMinutes(10);
        ReactivePaymentRequestInfoRedisTemplateWrapper paymentRequestInfoRedisTemplateWrapper = ReactiveRedisTemplateWrapperBuilder
                .buildPaymentRequestInfoRedisTemplateWrapper(reactiveRedisConnectionFactory, ttl);
        assertNotNull(paymentRequestInfoRedisTemplateWrapper);
        ReactiveRedisTemplate<String, PaymentRequestInfo> reactiveRedisTemplate = paymentRequestInfoRedisTemplateWrapper
                .unwrap();
        assertNotNull(reactiveRedisTemplate);

        PaymentRequestInfo paymentRequestInfo = new PaymentRequestInfo(
                new RptId("77777777777302016432223611415"),
                "77777777777",
                "companyName",
                "Pagamento di Test",
                12000,
                "2021-07-31",
                "1fb8539bdbc94123849a21be8eead8dd",
                "2021-07-31",
                null,
                null,
                null,
                null
        );

        ByteBuffer actual = reactiveRedisTemplate.getSerializationContext().getValueSerializationPair().getWriter()
                .write(paymentRequestInfo);

        ByteBuffer expected = ByteBuffer
                .wrap(buildJackson2RedisSerializer(PaymentRequestInfo.class).serialize(paymentRequestInfo));

        assertEquals(actual, expected);
    }

    @Test
    void shouldReadBuildPaymentRequestWrapperSuccessfully() {
        Duration ttl = Duration.ofMinutes(10);
        ReactivePaymentRequestInfoRedisTemplateWrapper paymentRequestInfoRedisTemplateWrapper = ReactiveRedisTemplateWrapperBuilder
                .buildPaymentRequestInfoRedisTemplateWrapper(reactiveRedisConnectionFactory, ttl);
        assertNotNull(paymentRequestInfoRedisTemplateWrapper);
        ReactiveRedisTemplate<String, PaymentRequestInfo> reactiveRedisTemplate = paymentRequestInfoRedisTemplateWrapper
                .unwrap();
        assertNotNull(reactiveRedisTemplate);

        PaymentRequestInfo paymentRequestInfo = new PaymentRequestInfo(
                new RptId("77777777777302016432223611415"),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        ByteBuffer expected = ByteBuffer
                .wrap(buildJackson2RedisSerializer(PaymentRequestInfo.class).serialize(paymentRequestInfo));

        PaymentRequestInfo actual = reactiveRedisTemplate.getSerializationContext().getValueSerializationPair()
                .getReader().read(expected);

        assertEquals(actual, paymentRequestInfo);
    }

    private <T> Jackson2JsonRedisSerializer<T> buildJackson2RedisSerializer(Class<T> clazz) {
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
