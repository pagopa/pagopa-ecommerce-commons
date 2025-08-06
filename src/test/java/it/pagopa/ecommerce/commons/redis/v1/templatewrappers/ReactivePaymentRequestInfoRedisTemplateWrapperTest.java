package it.pagopa.ecommerce.commons.redis.v1.templatewrappers;

import it.pagopa.ecommerce.commons.redis.reactivetemplatewrappers.v1.ReactivePaymentRequestInfoRedisTemplateWrapper;
import it.pagopa.ecommerce.commons.redis.templatewrappers.v1.PaymentRequestInfoRedisTemplateWrapper;
import it.pagopa.ecommerce.commons.repositories.v1.PaymentRequestInfo;
import it.pagopa.ecommerce.commons.v1.TransactionTestUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;

class ReactivePaymentRequestInfoRedisTemplateWrapperTest {

    private final ReactiveRedisTemplate<String, PaymentRequestInfo> reactiveRedisTemplate = Mockito
            .mock(ReactiveRedisTemplate.class);

    private final ReactiveValueOperations<String, PaymentRequestInfo> reactiveValueOperations = Mockito
            .mock(ReactiveValueOperations.class);

    private final ReactiveStreamOperations<String, String, PaymentRequestInfo> reactiveStreamOperations = Mockito
            .mock(ReactiveStreamOperations.class);

    private final String keyspace = "keys";

    private final Duration ttl = Duration.ofMinutes(10);

    private final ReactivePaymentRequestInfoRedisTemplateWrapper paymentRequestInfoRedisTemplateWrapper = new ReactivePaymentRequestInfoRedisTemplateWrapper(
            reactiveRedisTemplate,
            keyspace,
            ttl
    );

    @Test
    void shouldGetEntitySuccessfully() {
        // assertions
        Mono<PaymentRequestInfo> expected = Mono.just(TransactionTestUtils.paymentRequestInfoV1());
        Mockito.when(reactiveRedisTemplate.opsForValue()).thenReturn(reactiveValueOperations);
        Mockito.when(reactiveValueOperations.get("keys:%s".formatted(TransactionTestUtils.RPT_ID)))
                .thenReturn(expected);

        // test
        Mono<PaymentRequestInfo> actual = paymentRequestInfoRedisTemplateWrapper
                .findById(TransactionTestUtils.RPT_ID);
        Mockito.verify(reactiveValueOperations, Mockito.times(1)).get("keys:%s".formatted(TransactionTestUtils.RPT_ID));
        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    @Test
    void shouldReturnOptionalEmptyForMissingKey() {
        // assertions
        Mockito.when(reactiveRedisTemplate.opsForValue()).thenReturn(reactiveValueOperations);
        Mockito.when(reactiveValueOperations.get("keys:%s".formatted(TransactionTestUtils.RPT_ID))).thenReturn(null);

        // test
        Mono<PaymentRequestInfo> actual = paymentRequestInfoRedisTemplateWrapper
                .findById(TransactionTestUtils.RPT_ID);
        Mockito.verify(reactiveValueOperations, Mockito.times(1)).get("keys:%s".formatted(TransactionTestUtils.RPT_ID));
        assertNotNull(actual);
    }

    @Test
    void shouldSaveEntitySuccessfully() {
        // assertions
        PaymentRequestInfo paymentRequestInfo = TransactionTestUtils.paymentRequestInfoV1();
        Mockito.when(reactiveRedisTemplate.opsForValue()).thenReturn(reactiveValueOperations);
        Mockito.doNothing().when(reactiveValueOperations)
                .set("keys:%s".formatted(TransactionTestUtils.RPT_ID), paymentRequestInfo, ttl);
        // test
        paymentRequestInfoRedisTemplateWrapper.save(paymentRequestInfo);

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
        Boolean deleteResult = paymentRequestInfoRedisTemplateWrapper.deleteById(TransactionTestUtils.RPT_ID).block();
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

        Mono<Duration> ttl = paymentRequestInfoRedisTemplateWrapper.getTTL(TransactionTestUtils.RPT_ID);
        Mockito.verify(reactiveRedisTemplate, Mockito.times(1))
                .getExpire("keys:%s".formatted(TransactionTestUtils.RPT_ID));
        assertEquals(entityExpiration, ttl);
    }

    @Test
    void shouldReturnTTLForNullTTLReturnedByRedis() {
        Duration expectedDuration = Duration.ofSeconds(-3);
        Mockito.when(reactiveRedisTemplate.getExpire("keys:%s".formatted(TransactionTestUtils.RPT_ID)))
                .thenReturn(null);

        Mono<Duration> ttl = paymentRequestInfoRedisTemplateWrapper.getTTL(TransactionTestUtils.RPT_ID);
        Mockito.verify(reactiveRedisTemplate, Mockito.times(1))
                .getExpire("keys:%s".formatted(TransactionTestUtils.RPT_ID));
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
        PaymentRequestInfo paymentRequestInfo = TransactionTestUtils.paymentRequestInfoV1();
        Mockito.when(reactiveRedisTemplate.opsForValue()).thenReturn(reactiveValueOperations);
        Mockito.doNothing().when(reactiveValueOperations)
                .set("keys:%s".formatted(TransactionTestUtils.RPT_ID), paymentRequestInfo, customTTL);
        // test
        paymentRequestInfoRedisTemplateWrapper.save(paymentRequestInfo, customTTL).block();

        // assertions
        Mockito.verify(reactiveValueOperations, Mockito.times(1))
                .set("keys:%s".formatted(TransactionTestUtils.RPT_ID), paymentRequestInfo, customTTL);
    }

    @Test
    void shouldSaveIfAbsentEntitySuccessfully() {
        // assertions
        PaymentRequestInfo paymentRequestInfo = TransactionTestUtils.paymentRequestInfoV1();
        Mockito.when(reactiveRedisTemplate.opsForValue()).thenReturn(reactiveValueOperations);
        Mockito.when(
                reactiveValueOperations
                        .setIfAbsent("keys:%s".formatted(TransactionTestUtils.RPT_ID), paymentRequestInfo, ttl)
        ).thenReturn(Mono.just(true));
        // test
        paymentRequestInfoRedisTemplateWrapper.saveIfAbsent(paymentRequestInfo).block();
        // assertions
        Mockito.verify(reactiveValueOperations, Mockito.times(1))
                .setIfAbsent("keys:%s".formatted(TransactionTestUtils.RPT_ID), paymentRequestInfo, ttl);
    }

    @Test
    void shouldSaveIfAbsentEntityWithCustomTTLSuccessfully() {
        // assertions
        Duration customTTL = Duration.ofMillis(100);
        PaymentRequestInfo paymentRequestInfo = TransactionTestUtils.paymentRequestInfoV1();
        Mockito.when(reactiveRedisTemplate.opsForValue()).thenReturn(reactiveValueOperations);
        Mockito.when(
                reactiveValueOperations
                        .setIfAbsent("keys:%s".formatted(TransactionTestUtils.RPT_ID), paymentRequestInfo, customTTL)
        ).thenReturn(Mono.just(true));
        // test
        paymentRequestInfoRedisTemplateWrapper.saveIfAbsent(paymentRequestInfo, customTTL).block();

        // assertions
        Mockito.verify(reactiveValueOperations, Mockito.times(1))
                .setIfAbsent("keys:%s".formatted(TransactionTestUtils.RPT_ID), paymentRequestInfo, customTTL);
    }

    @Test
    void shouldRetrieveAllKeysInKeyspaceSuccessfully() {
        // assertions
        Set<String> keys = Set.of("keys:1", "keys:2");
        Mockito.when(reactiveRedisTemplate.keys("keys*")).thenReturn((Flux<String>) keys);
        // test
        Flux<String> returnedKeys = paymentRequestInfoRedisTemplateWrapper.keysInKeyspace();

        // assertions
        Mockito.verify(reactiveRedisTemplate, Mockito.times(1))
                .keys("keys*");
        assertEquals(keys, returnedKeys);
    }

    @Test
    void shouldRetrieveAllValuesInKeyspaceSuccessfully() {
        // given
        Set<String> keys = Set.of("keys:1", "keys:2");
        PaymentRequestInfo value1 = TransactionTestUtils.paymentRequestInfoV1();
        PaymentRequestInfo value2 = TransactionTestUtils.paymentRequestInfoV1();

        Mockito.when(reactiveRedisTemplate.keys("keys*"))
                .thenReturn(Flux.fromIterable(keys));

        Mockito.when(reactiveRedisTemplate.opsForValue())
                .thenReturn(reactiveValueOperations);

        Mockito.when(reactiveValueOperations.get("keys:1"))
                .thenReturn(Mono.just(value1));
        Mockito.when(reactiveValueOperations.get("keys:2"))
                .thenReturn(Mono.just(value2));

        // when
        Flux<PaymentRequestInfo> result = paymentRequestInfoRedisTemplateWrapper.getAllValuesInKeySpace();

        // then
        StepVerifier.create(result)
                .expectNext(value1)
                .expectNext(value2)
                .verifyComplete();

        Mockito.verify(reactiveRedisTemplate).keys("keys*");
        Mockito.verify(reactiveValueOperations).get("keys:1");
        Mockito.verify(reactiveValueOperations).get("keys:2");
    }

    @Test
    void shouldWriteEventToStreamSuccessfully() {
        // arrange
        String streamKey = "streamKey";
        PaymentRequestInfo paymentRequestInfo = TransactionTestUtils.paymentRequestInfoV1();
        RecordId expectedRecordId = RecordId.of(System.currentTimeMillis(), 0);

        Mockito.when(reactiveRedisTemplate.opsForStream())
                .thenReturn(reactiveStreamOperations);

        Mockito.when(reactiveStreamOperations.add(Mockito.argThat(record -> {
            if (record instanceof ObjectRecord<?, ?> objRecord) {
                return objRecord.getValue().equals(paymentRequestInfo) && objRecord.getStream().equals(streamKey);
            }
            return false;
        }))).thenReturn(Mono.just(expectedRecordId));

        // act
        Mono<RecordId> result = paymentRequestInfoRedisTemplateWrapper
                .writeEventToStream(streamKey, paymentRequestInfo);

        // assert
        StepVerifier.create(result)
                .expectNext(expectedRecordId)
                .verifyComplete();

        Mockito.verify(reactiveStreamOperations, Mockito.times(1))
                .add(any(ObjectRecord.class));
    }

    @Test
    void shouldWriteEventToStreamSuccessfullyTrimmingPreviousEvents() {
        // arrange
        String streamKey = "streamKey";
        long streamSize = 0;
        PaymentRequestInfo paymentRequestInfo = TransactionTestUtils.paymentRequestInfoV1();
        RecordId expectedRecordId = RecordId.of(System.currentTimeMillis(), 0);

        OngoingStubbing<ReactiveStreamOperations<String, Object, Object>> reactiveStreamOperationsOngoingStubbing = Mockito
                .when(reactiveRedisTemplate.opsForStream())
                .thenReturn(reactiveStreamOperations);

        Mockito.when(reactiveStreamOperations.trim(streamKey, streamSize))
                .thenReturn(Mono.just(0L));

        Mockito.when(reactiveStreamOperations.add(Mockito.argThat(record -> {
            if (record instanceof ObjectRecord<?, ?> objRecord) {
                return objRecord.getValue().equals(paymentRequestInfo);
            }
            return false;
        }))).thenReturn(Mono.just(expectedRecordId));

        // act
        Mono<RecordId> result = paymentRequestInfoRedisTemplateWrapper
                .writeEventToStreamTrimmingEvents(streamKey, paymentRequestInfo, streamSize);

        // assert
        StepVerifier.create(result)
                .expectNext(expectedRecordId)
                .verifyComplete();

        Mockito.verify(reactiveStreamOperations, Mockito.times(1)).trim(streamKey, streamSize);
        Mockito.verify(reactiveStreamOperations, Mockito.times(1)).add(any(ObjectRecord.class));
    }

    @Test
    void shouldTrimEventsSuccessfully() {
        // arrange
        String streamKey = "streamKey";
        long streamSize = 0L;

        Mockito.when(reactiveRedisTemplate.opsForStream())
                .thenReturn(reactiveStreamOperations);

        Mockito.when(reactiveStreamOperations.trim(streamKey, streamSize))
                .thenReturn(Mono.just(0L));

        // act
        Mono<Long> result = paymentRequestInfoRedisTemplateWrapper
                .trimEvents(streamKey, streamSize);

        // assert
        StepVerifier.create(result)
                .expectNext(0L)
                .verifyComplete();

        Mockito.verify(reactiveStreamOperations, Mockito.times(1))
                .trim(streamKey, streamSize);
    }

    @Test
    void shouldThrowExceptionWritingEventToStreamWithInvalidStreamSize() {
        // assertions
        String streamKey = "streamKey";
        int streamSize = -1;
        PaymentRequestInfo paymentRequestInfo = TransactionTestUtils.paymentRequestInfoV1();
        Mockito.when(reactiveRedisTemplate.opsForStream()).thenReturn((StreamOperations) reactiveStreamOperations);
        Mockito.when(reactiveRedisTemplate.opsForStream())
                .thenReturn(reactiveStreamOperations);

        Mockito.when(reactiveStreamOperations.trim(streamKey, streamSize)).thenReturn(Mono.just(0L));
        Mockito.when(reactiveStreamOperations.add(argThat(r -> {
            ObjectRecord record = (ObjectRecord) r;
            return record.getValue().equals(paymentRequestInfo);
        }))).thenReturn(RecordId.of(System.currentTimeMillis(), 0));
        // test
        assertThrows(
                IllegalArgumentException.class,
                () -> paymentRequestInfoRedisTemplateWrapper
                        .writeEventToStreamTrimmingEvents(streamKey, paymentRequestInfo, streamSize)
        );

        // assertions
        Mockito.verify(reactiveStreamOperations, Mockito.times(0))
                .add(any(ObjectRecord.class));
        Mockito.verify(reactiveStreamOperations, Mockito.times(0))
                .trim(streamKey, streamSize);
    }

    @Test
    void shouldCreateStreamEventGroupSuccessfully() {
        // assertions
        String streamKey = "streamKey";
        String groupName = "groupName";
        Mockito.when(reactiveRedisTemplate.opsForStream())
                .thenReturn((ReactiveStreamOperations) reactiveStreamOperations);
        Mockito.when(reactiveStreamOperations.createGroup(streamKey, groupName)).thenReturn(Mono.just("OK"));
        // test
        String outcome = paymentRequestInfoRedisTemplateWrapper.createGroup(streamKey, groupName).block();

        // assertions
        Mockito.verify(reactiveStreamOperations, Mockito.times(1))
                .createGroup(streamKey, groupName);
        assertEquals("OK", outcome);
    }

    @Test
    void shouldCreateStreamEventGroupSuccessfullyWithCustomReadOffset() {
        // assertions
        String streamKey = "streamKey";
        String groupName = "groupName";
        ReadOffset offset = ReadOffset.from("0-0");
        Mockito.when(reactiveRedisTemplate.opsForStream())
                .thenReturn((ReactiveStreamOperations) reactiveStreamOperations);
        Mockito.when(reactiveStreamOperations.createGroup(streamKey, offset, groupName)).thenReturn(Mono.just("OK"));
        // test
        String outcome = paymentRequestInfoRedisTemplateWrapper.createGroup(streamKey, groupName, offset).block();

        // assertions
        Mockito.verify(reactiveStreamOperations, Mockito.times(1))
                .createGroup(streamKey, offset, groupName);
        assertEquals("OK", outcome);
    }

    @Test
    void shouldDestroyStreamEventGroupSuccessfully() {
        // assertions
        String streamKey = "streamKey";
        String groupName = "groupName";
        Mockito.when(reactiveRedisTemplate.opsForStream())
                .thenReturn((ReactiveStreamOperations) reactiveStreamOperations);
        Mockito.when(reactiveStreamOperations.destroyGroup(streamKey, groupName)).thenReturn(Boolean.TRUE);
        // test
        Mono<Boolean> outcome = paymentRequestInfoRedisTemplateWrapper.destroyGroup(streamKey, groupName).block();

        // assertions
        Mockito.verify(reactiveStreamOperations, Mockito.times(1))
                .destroyGroup(streamKey, groupName);
        assertEquals(Boolean.TRUE, outcome);
    }

    @Test
    void shouldAcknowledgeEventSuccessfully() {
        // assertions
        String streamKey = "streamKey";
        String groupId = "group";
        String[] ids = {
                "id1",
                "id2"
        };
        int streamSize = 0;
        Mockito.when(reactiveRedisTemplate.opsForStream()).thenReturn((StreamOperations) reactiveStreamOperations);
        Mockito.when(reactiveStreamOperations.acknowledge(streamKey, groupId, ids)).thenReturn(Mono.just(0L));
        // test
        paymentRequestInfoRedisTemplateWrapper
                .acknowledgeEvents(streamKey, groupId, ids);

        // assertions
        Mockito.verify(reactiveStreamOperations, Mockito.times(1))
                .acknowledge(streamKey, groupId, ids);
    }

}
