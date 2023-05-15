package it.pagopa.ecommerce.commons.redis.templatewrappers;

import it.pagopa.ecommerce.commons.repositories.PaymentRequestInfo;
import it.pagopa.ecommerce.commons.v1.TransactionTestUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentRequestInfoRedisTemplateWrapperTest {

    private final RedisTemplate<String, PaymentRequestInfo> redisTemplate = Mockito.mock(RedisTemplate.class);

    private final ValueOperations<String, PaymentRequestInfo> valueOperations = Mockito.mock(ValueOperations.class);

    private final String keyspace = "keys";

    private final Duration ttl = Duration.ofMinutes(10);

    private final PaymentRequestInfoRedisTemplateWrapper paymentRequestInfoRedisTemplateWrapper = new PaymentRequestInfoRedisTemplateWrapper(
            redisTemplate,
            keyspace,
            ttl
    );

    @Test
    void shouldGetEntitySuccessfully() {
        // assertions
        PaymentRequestInfo expected = TransactionTestUtils.paymentRequestInfo();
        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        Mockito.when(valueOperations.get("keys:%s".formatted(TransactionTestUtils.RPT_ID))).thenReturn(expected);

        // test
        Optional<PaymentRequestInfo> actual = paymentRequestInfoRedisTemplateWrapper
                .findById(TransactionTestUtils.RPT_ID);
        Mockito.verify(valueOperations, Mockito.times(1)).get("keys:%s".formatted(TransactionTestUtils.RPT_ID));
        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
    }

    @Test
    void shouldReturnOptionalEmptyForMissingKey() {
        // assertions
        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        Mockito.when(valueOperations.get("keys:%s".formatted(TransactionTestUtils.RPT_ID))).thenReturn(null);

        // test
        Optional<PaymentRequestInfo> actual = paymentRequestInfoRedisTemplateWrapper
                .findById(TransactionTestUtils.RPT_ID);
        Mockito.verify(valueOperations, Mockito.times(1)).get("keys:%s".formatted(TransactionTestUtils.RPT_ID));
        assertTrue(actual.isEmpty());
    }

    @Test
    void shouldSaveEntitySuccessfully() {
        // assertions
        PaymentRequestInfo paymentRequestInfo = TransactionTestUtils.paymentRequestInfo();
        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        Mockito.doNothing().when(valueOperations)
                .set("keys:%s".formatted(TransactionTestUtils.RPT_ID), paymentRequestInfo, ttl);
        // test
        paymentRequestInfoRedisTemplateWrapper.save(paymentRequestInfo);

        // assertions
        Mockito.verify(valueOperations, Mockito.times(1))
                .set("keys:%s".formatted(TransactionTestUtils.RPT_ID), paymentRequestInfo, ttl);
    }

    @Test
    void shouldDeleteEntitySuccessfully() {
        // assertions
        PaymentRequestInfo expected = TransactionTestUtils.paymentRequestInfo();
        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        Mockito.when(valueOperations.getAndDelete("keys:%s".formatted(TransactionTestUtils.RPT_ID)))
                .thenReturn(expected);
        // test
        PaymentRequestInfo actual = paymentRequestInfoRedisTemplateWrapper.deleteById(TransactionTestUtils.RPT_ID);

        // assertions
        Mockito.verify(valueOperations, Mockito.times(1))
                .getAndDelete("keys:%s".formatted(TransactionTestUtils.RPT_ID));
        assertEquals(expected, actual);
    }
}
