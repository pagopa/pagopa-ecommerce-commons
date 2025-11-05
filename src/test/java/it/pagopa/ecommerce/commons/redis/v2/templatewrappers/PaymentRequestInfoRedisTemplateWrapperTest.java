package it.pagopa.ecommerce.commons.redis.v2.templatewrappers;

import it.pagopa.ecommerce.commons.redis.templatewrappers.v2.PaymentRequestInfoRedisTemplateWrapper;
import it.pagopa.ecommerce.commons.repositories.v2.PaymentRequestInfo;
import it.pagopa.ecommerce.commons.v2.TransactionTestUtils;
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
        PaymentRequestInfo expected = TransactionTestUtils.paymentRequestInfoV2();
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
        PaymentRequestInfo paymentRequestInfo = TransactionTestUtils.paymentRequestInfoV2();
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
        Mockito.when(redisTemplate.delete("keys:%s".formatted(TransactionTestUtils.RPT_ID)))
                .thenReturn(Boolean.TRUE);
        // test
        Boolean deleteResult = paymentRequestInfoRedisTemplateWrapper.deleteById(TransactionTestUtils.RPT_ID);
        // assertions
        Mockito.verify(redisTemplate, Mockito.times(1))
                .delete("keys:%s".formatted(TransactionTestUtils.RPT_ID));
        assertTrue(deleteResult);
    }

    @Test
    void shouldReturnTTLSuccessfully() {
        Duration entityExpiration = Duration.ofSeconds(10);
        Mockito.when(redisTemplate.getExpire("keys:%s".formatted(TransactionTestUtils.RPT_ID)))
                .thenReturn(entityExpiration.getSeconds());

        Duration ttl = paymentRequestInfoRedisTemplateWrapper.getTTL(TransactionTestUtils.RPT_ID);
        Mockito.verify(redisTemplate, Mockito.times(1)).getExpire("keys:%s".formatted(TransactionTestUtils.RPT_ID));
        assertEquals(entityExpiration, ttl);
    }

    @Test
    void shouldReturnTTLForNullTTLReturnedByRedis() {
        Duration expectedDuration = Duration.ofSeconds(-3);
        Mockito.when(redisTemplate.getExpire("keys:%s".formatted(TransactionTestUtils.RPT_ID)))
                .thenReturn(null);

        Duration ttl = paymentRequestInfoRedisTemplateWrapper.getTTL(TransactionTestUtils.RPT_ID);
        Mockito.verify(redisTemplate, Mockito.times(1)).getExpire("keys:%s".formatted(TransactionTestUtils.RPT_ID));
        assertEquals(expectedDuration, ttl);
    }

    @Test
    void shouldReturnRepositoryDefaultTTL() {
        assertEquals(ttl, paymentRequestInfoRedisTemplateWrapper.getDefaultTTL());
    }

    @Test
    void shouldSaveEntityWithCustomTTLSuccessfully() {
        // assertions
        Duration customTTL = Duration.ofMillis(100);
        PaymentRequestInfo paymentRequestInfo = TransactionTestUtils.paymentRequestInfoV2();
        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        Mockito.doNothing().when(valueOperations)
                .set("keys:%s".formatted(TransactionTestUtils.RPT_ID), paymentRequestInfo, customTTL);
        // test
        paymentRequestInfoRedisTemplateWrapper.save(paymentRequestInfo, customTTL);

        // assertions
        Mockito.verify(valueOperations, Mockito.times(1))
                .set("keys:%s".formatted(TransactionTestUtils.RPT_ID), paymentRequestInfo, customTTL);
    }

    @Test
    void shouldSaveIfAbsentEntitySuccessfully() {
        // assertions
        PaymentRequestInfo paymentRequestInfo = it.pagopa.ecommerce.commons.v1.TransactionTestUtils
                .paymentRequestInfoV2();
        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        Mockito.when(
                valueOperations.setIfAbsent(
                        "keys:%s".formatted(it.pagopa.ecommerce.commons.v1.TransactionTestUtils.RPT_ID),
                        paymentRequestInfo,
                        ttl
                )
        ).thenReturn(true);

        // test
        paymentRequestInfoRedisTemplateWrapper.saveIfAbsent(paymentRequestInfo);

        // assertions
        Mockito.verify(valueOperations, Mockito.times(1))
                .setIfAbsent(
                        "keys:%s".formatted(it.pagopa.ecommerce.commons.v1.TransactionTestUtils.RPT_ID),
                        paymentRequestInfo,
                        ttl
                );
    }

    @Test
    void shouldSaveIfAbsentEntityWithCustomTTLSuccessfully() {
        // assertions
        Duration customTTL = Duration.ofMillis(100);
        PaymentRequestInfo paymentRequestInfo = it.pagopa.ecommerce.commons.v1.TransactionTestUtils
                .paymentRequestInfoV2();
        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        Mockito.when(
                valueOperations.setIfAbsent(
                        "keys:%s".formatted(it.pagopa.ecommerce.commons.v1.TransactionTestUtils.RPT_ID),
                        paymentRequestInfo,
                        customTTL
                )
        ).thenReturn(true);

        // test
        paymentRequestInfoRedisTemplateWrapper.saveIfAbsent(paymentRequestInfo, customTTL);

        // assertions
        Mockito.verify(valueOperations, Mockito.times(1))
                .setIfAbsent(
                        "keys:%s".formatted(it.pagopa.ecommerce.commons.v1.TransactionTestUtils.RPT_ID),
                        paymentRequestInfo,
                        customTTL
                );
    }
}
