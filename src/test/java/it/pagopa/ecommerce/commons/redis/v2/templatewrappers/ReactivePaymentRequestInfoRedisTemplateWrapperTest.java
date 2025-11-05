package it.pagopa.ecommerce.commons.redis.v2.templatewrappers;

import it.pagopa.ecommerce.commons.redis.reactivetemplatewrappers.v2.ReactivePaymentRequestInfoRedisTemplateWrapper;
import it.pagopa.ecommerce.commons.repositories.v2.PaymentRequestInfo;
import it.pagopa.ecommerce.commons.v2.TransactionTestUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class ReactivePaymentRequestInfoRedisTemplateWrapperTest {

    private final ReactiveRedisTemplate<String, PaymentRequestInfo> reactiveRedisTemplate = Mockito
            .mock(ReactiveRedisTemplate.class);

    private final ReactiveValueOperations<String, PaymentRequestInfo> reactiveValueOperations = Mockito
            .mock(ReactiveValueOperations.class);

    private final String keyspace = "keys";

    private final Duration ttl = Duration.ofMinutes(10);

    private final ReactivePaymentRequestInfoRedisTemplateWrapper paymentRequestInforeactiveRedisTemplateWrapper = new ReactivePaymentRequestInfoRedisTemplateWrapper(
            reactiveRedisTemplate,
            keyspace,
            ttl
    );

    @Test
    void shouldGetEntitySuccessfully() {
        // assertions
        PaymentRequestInfo expected = TransactionTestUtils.paymentRequestInfoV2();
        Mockito.when(reactiveRedisTemplate.opsForValue()).thenReturn(reactiveValueOperations);
        Mockito.when(reactiveValueOperations.get("keys:%s".formatted(TransactionTestUtils.RPT_ID)))
                .thenReturn(Mono.just(expected));

        // test
        Mono<PaymentRequestInfo> actual = paymentRequestInforeactiveRedisTemplateWrapper
                .findById(TransactionTestUtils.RPT_ID);
        Mockito.verify(reactiveValueOperations, Mockito.times(1)).get("keys:%s".formatted(TransactionTestUtils.RPT_ID));
        assertNotNull(actual);
        assertEquals(expected, actual.block());
    }

    @Test
    void shouldReturnOptionalEmptyForMissingKey() {
        // assertions
        Mockito.when(reactiveRedisTemplate.opsForValue()).thenReturn(reactiveValueOperations);
        Mockito.when(reactiveValueOperations.get("keys:%s".formatted(TransactionTestUtils.RPT_ID))).thenReturn(null);

        // test
        Mono<PaymentRequestInfo> actual = paymentRequestInforeactiveRedisTemplateWrapper
                .findById(TransactionTestUtils.RPT_ID);
        Mockito.verify(reactiveValueOperations, Mockito.times(1)).get("keys:%s".formatted(TransactionTestUtils.RPT_ID));
        assertNull(actual);
    }

    @Test
    void shouldSaveEntitySuccessfully() {
        // assertions
        PaymentRequestInfo paymentRequestInfo = TransactionTestUtils.paymentRequestInfoV2();
        Mockito.when(reactiveRedisTemplate.opsForValue()).thenReturn(reactiveValueOperations);
        Mockito.when(
                reactiveValueOperations.set("keys:%s".formatted(TransactionTestUtils.RPT_ID), paymentRequestInfo, ttl)
        )
                .thenReturn(Mono.just(true));

        // test
        paymentRequestInforeactiveRedisTemplateWrapper.save(paymentRequestInfo);

        // assertions
        Mockito.verify(reactiveValueOperations, Mockito.times(1))
                .set("keys:%s".formatted(TransactionTestUtils.RPT_ID), paymentRequestInfo, ttl);
    }

    @Test
    void shouldDeleteEntitySuccessfully() {
        // assertions
        Mockito.when(reactiveRedisTemplate.delete("keys:%s".formatted(TransactionTestUtils.RPT_ID)))
                .thenReturn(Mono.just(1L));
        // test
        Boolean deleteResult = paymentRequestInforeactiveRedisTemplateWrapper.deleteById(TransactionTestUtils.RPT_ID)
                .block();
        // assertions
        Mockito.verify(reactiveRedisTemplate, Mockito.times(1))
                .delete("keys:%s".formatted(TransactionTestUtils.RPT_ID));
        assertEquals(Boolean.TRUE, deleteResult);
    }

    @Test
    void shouldReturnTTLSuccessfully() {
        Duration entityExpiration = Duration.ofSeconds(10);
        Mockito.when(reactiveRedisTemplate.getExpire("keys:%s".formatted(TransactionTestUtils.RPT_ID)))
                .thenReturn(Mono.just(entityExpiration));

        Duration ttl = paymentRequestInforeactiveRedisTemplateWrapper.getTTL(TransactionTestUtils.RPT_ID).block();
        Mockito.verify(reactiveRedisTemplate, Mockito.times(1))
                .getExpire("keys:%s".formatted(TransactionTestUtils.RPT_ID));
        assertEquals(entityExpiration, ttl);
    }

    @Test
    void shouldReturnTTLForNullTTLReturnedByRedis() {
        Mockito.when(reactiveRedisTemplate.getExpire("keys:%s".formatted(TransactionTestUtils.RPT_ID)))
                .thenReturn(Mono.empty());

        Mono<Duration> ttl = paymentRequestInforeactiveRedisTemplateWrapper.getTTL(TransactionTestUtils.RPT_ID);
        Mockito.verify(reactiveRedisTemplate, Mockito.times(1))
                .getExpire("keys:%s".formatted(TransactionTestUtils.RPT_ID));
        assertEquals(Mono.empty(), ttl);
    }

    @Test
    void shouldReturnRepositoryDefaultTTL() {
        assertEquals(ttl, paymentRequestInforeactiveRedisTemplateWrapper.getDefaultTTL());
    }

    @Test
    void shouldSaveEntityWithCustomTTLSuccessfully() {
        // assertions
        Duration customTTL = Duration.ofMillis(100);
        PaymentRequestInfo paymentRequestInfo = TransactionTestUtils.paymentRequestInfoV2();
        Mockito.when(reactiveRedisTemplate.opsForValue()).thenReturn(reactiveValueOperations);
        Mockito.when(
                reactiveValueOperations.set(
                        "keys:%s".formatted(TransactionTestUtils.RPT_ID),
                        paymentRequestInfo,
                        customTTL
                )
        ).thenReturn(Mono.just(true));

        // test
        paymentRequestInforeactiveRedisTemplateWrapper.save(paymentRequestInfo, customTTL).block();

        // assertions
        Mockito.verify(reactiveValueOperations, Mockito.times(1))
                .set("keys:%s".formatted(TransactionTestUtils.RPT_ID), paymentRequestInfo, customTTL);
    }

    @Test
    void shouldSaveIfAbsentEntitySuccessfully() {
        // assertions
        PaymentRequestInfo paymentRequestInfo = it.pagopa.ecommerce.commons.v1.TransactionTestUtils
                .paymentRequestInfoV2();
        Mockito.when(reactiveRedisTemplate.opsForValue()).thenReturn(reactiveValueOperations);
        Mockito.when(
                reactiveValueOperations.setIfAbsent(
                        "keys:%s".formatted(it.pagopa.ecommerce.commons.v1.TransactionTestUtils.RPT_ID),
                        paymentRequestInfo,
                        ttl
                )
        ).thenReturn(Mono.just(true));

        // test
        paymentRequestInforeactiveRedisTemplateWrapper.saveIfAbsent(paymentRequestInfo).block();

        // assertions
        Mockito.verify(reactiveValueOperations, Mockito.times(1))
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
        Mockito.when(reactiveRedisTemplate.opsForValue()).thenReturn(reactiveValueOperations);
        Mockito.when(
                reactiveValueOperations.setIfAbsent(
                        "keys:%s".formatted(it.pagopa.ecommerce.commons.v1.TransactionTestUtils.RPT_ID),
                        paymentRequestInfo,
                        customTTL
                )
        ).thenReturn(Mono.just(true));

        // test
        paymentRequestInforeactiveRedisTemplateWrapper.saveIfAbsent(paymentRequestInfo, customTTL).block();

        // assertions
        Mockito.verify(reactiveValueOperations, Mockito.times(1))
                .setIfAbsent(
                        "keys:%s".formatted(it.pagopa.ecommerce.commons.v1.TransactionTestUtils.RPT_ID),
                        paymentRequestInfo,
                        customTTL
                );
    }
}
