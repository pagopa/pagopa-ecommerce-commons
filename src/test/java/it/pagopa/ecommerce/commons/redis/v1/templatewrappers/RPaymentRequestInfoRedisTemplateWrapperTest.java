package it.pagopa.ecommerce.commons.redis.v1.templatewrappers;

import it.pagopa.ecommerce.commons.redis.reactivetemplatewrappers.v1.ReactivePaymentRequestInfoRedisTemplateWrapper;
import it.pagopa.ecommerce.commons.redis.templatewrappers.v1.PaymentRequestInfoRedisTemplateWrapper;
import it.pagopa.ecommerce.commons.repositories.v1.PaymentRequestInfo;
import it.pagopa.ecommerce.commons.v1.TransactionTestUtils;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
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

class RPaymentRequestInfoRedisTemplateWrapperTest {

    private final ReactiveRedisTemplate<String, PaymentRequestInfo> redisTemplate = Mockito.mock(ReactiveRedisTemplate.class);

    private final ReactiveValueOperations<String, PaymentRequestInfo> valueOperations = Mockito.mock(ReactiveValueOperations.class);

    private final ReactiveStreamOperations<String, String, PaymentRequestInfo> streamOperations = Mockito
            .mock(ReactiveStreamOperations.class);

    private final String keyspace = "keys";

    private final Duration ttl = Duration.ofMinutes(10);

    private final ReactivePaymentRequestInfoRedisTemplateWrapper paymentRequestInfoRedisTemplateWrapper = new ReactivePaymentRequestInfoRedisTemplateWrapper(
            redisTemplate,
            keyspace,
            ttl
    );

    @Test
    void shouldGetEntitySuccessfully() {
        // assertions
        PaymentRequestInfo expected = TransactionTestUtils.paymentRequestInfoV1();
        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        Mockito.when(valueOperations.get("keys:%s".formatted(TransactionTestUtils.RPT_ID))).thenReturn(Mono.just(expected));

        // test
        Mono<PaymentRequestInfo> actual = paymentRequestInfoRedisTemplateWrapper
                .findById(TransactionTestUtils.RPT_ID);
        Mockito.verify(valueOperations, Mockito.times(1)).get("keys:%s".formatted(TransactionTestUtils.RPT_ID));
        assertNotNull(actual);
        assertEquals(expected, actual.block());
    }

    @Test
    void shouldReturnEmptyMonoForMissingKey() {
        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        Mockito.when(valueOperations.get("keys:%s".formatted(TransactionTestUtils.RPT_ID)))
                .thenReturn(Mono.empty());

        Mono<PaymentRequestInfo> actual =
                paymentRequestInfoRedisTemplateWrapper.findById(TransactionTestUtils.RPT_ID);

        StepVerifier.create(actual)
                .verifyComplete();
        Mockito.verify(valueOperations).get("keys:%s".formatted(TransactionTestUtils.RPT_ID));
    }

    @Test
    void shouldSaveEntitySuccessfully() {
        // arrange
        PaymentRequestInfo paymentRequestInfo = TransactionTestUtils.paymentRequestInfoV1();
        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        String key = "keys:%s".formatted(TransactionTestUtils.RPT_ID);
        Mockito.when(valueOperations.set(key, paymentRequestInfo, ttl))
                .thenReturn(Mono.just(true));

        // act
        paymentRequestInfoRedisTemplateWrapper.save(paymentRequestInfo);

        // assert
        Mockito.verify(valueOperations, Mockito.times(1))
                .set(key, paymentRequestInfo, ttl);
    }


    @Test
    void shouldDeleteEntitySuccessfully() {
        // assertions
        Mockito.when(redisTemplate.delete("keys:%s".formatted(TransactionTestUtils.RPT_ID)))
                .thenReturn(Mono.just(1L));
        // test
        Boolean deleteResult = paymentRequestInfoRedisTemplateWrapper.deleteById(TransactionTestUtils.RPT_ID).block();
        // assertions
        Mockito.verify(redisTemplate, Mockito.times(1))
                .delete("keys:%s".formatted(TransactionTestUtils.RPT_ID));
        assertEquals(Boolean.TRUE, deleteResult);
    }

    @Test
    void shouldReturnTTLSuccessfully() {
        Duration entityExpiration = Duration.ofSeconds(10);
        Mockito.when(redisTemplate.getExpire("keys:%s".formatted(TransactionTestUtils.RPT_ID)))
                .thenReturn(Mono.just(Duration.ofSeconds(entityExpiration.getSeconds())));

        Duration ttl = paymentRequestInfoRedisTemplateWrapper.getTTL(TransactionTestUtils.RPT_ID).block();
        Mockito.verify(redisTemplate, Mockito.times(1)).getExpire("keys:%s".formatted(TransactionTestUtils.RPT_ID));
        assertEquals(entityExpiration, ttl);
    }

    @Test
    void shouldReturnTTLForNullTTLReturnedByRedis() {
        Duration expectedDuration = Duration.ofSeconds(-3);
        Mockito.when(redisTemplate.getExpire("keys:%s".formatted(TransactionTestUtils.RPT_ID)))
                .thenReturn(Mono.empty());

        Mono<Duration> ttl = paymentRequestInfoRedisTemplateWrapper.getTTL(TransactionTestUtils.RPT_ID);
        Mockito.verify(redisTemplate, Mockito.times(1))
                .getExpire("keys:%s".formatted(TransactionTestUtils.RPT_ID));
        assertEquals(expectedDuration, ttl.block());
    }

    @Test
    void shouldReturnRepositoryDefaultTTL() {
        assertEquals(ttl, paymentRequestInfoRedisTemplateWrapper.getDefaultTTL());
    }


    @Test
    void shouldSaveEntityWithCustomTTLSuccessfully() {
        // arrange
        Duration customTTL = Duration.ofMillis(100);
        PaymentRequestInfo paymentRequestInfo = TransactionTestUtils.paymentRequestInfoV1();

        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        Mockito.when(valueOperations.set(
                "keys:%s".formatted(TransactionTestUtils.RPT_ID),
                paymentRequestInfo,
                customTTL)
        ).thenReturn(Mono.just(true));

        // act
        paymentRequestInfoRedisTemplateWrapper.save(paymentRequestInfo, customTTL).block();

        // assert
        Mockito.verify(valueOperations, Mockito.times(1)).set(
                "keys:%s".formatted(TransactionTestUtils.RPT_ID),
                paymentRequestInfo,
                customTTL
        );
    }


    @Test
    void shouldSaveIfAbsentEntitySuccessfully() {
        // assertions
        PaymentRequestInfo paymentRequestInfo = TransactionTestUtils.paymentRequestInfoV1();
        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        Mockito.when(
                valueOperations.setIfAbsent("keys:%s".formatted(TransactionTestUtils.RPT_ID), paymentRequestInfo, ttl)
        ).thenReturn(Mono.just(true));
        // test
        paymentRequestInfoRedisTemplateWrapper.saveIfAbsent(paymentRequestInfo).block();
        // assertions
        Mockito.verify(valueOperations, Mockito.times(1))
                .setIfAbsent("keys:%s".formatted(TransactionTestUtils.RPT_ID), paymentRequestInfo, ttl);
    }

    @Test
    void shouldSaveIfAbsentEntityWithCustomTTLSuccessfully() {
        // assertions
        Duration customTTL = Duration.ofMillis(100);
        PaymentRequestInfo paymentRequestInfo = TransactionTestUtils.paymentRequestInfoV1();
        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        Mockito.when(
                valueOperations
                        .setIfAbsent("keys:%s".formatted(TransactionTestUtils.RPT_ID), paymentRequestInfo, customTTL)
        ).thenReturn(Mono.just(true));
        // test
        paymentRequestInfoRedisTemplateWrapper.saveIfAbsent(paymentRequestInfo, customTTL).block();

        // assertions
        Mockito.verify(valueOperations, Mockito.times(1))
                .setIfAbsent("keys:%s".formatted(TransactionTestUtils.RPT_ID), paymentRequestInfo, customTTL);
    }

    @Test
    void shouldRetrieveAllKeysInKeyspaceSuccessfully() {
        // assertions
        Set<String> keys = Set.of("keys:1", "keys:2");
        Mockito.when(redisTemplate.keys("keys*")).thenReturn(Flux.fromIterable(keys));
        // test
        Flux<String> returnedKeys = paymentRequestInfoRedisTemplateWrapper.keysInKeyspace();

        StepVerifier.create(returnedKeys)
                .recordWith(java.util.ArrayList::new)
                .expectNextCount(2)
                .consumeRecordedWith(list ->
                        org.assertj.core.api.Assertions.assertThat(list)
                                .containsExactlyInAnyOrder("keys:1", "keys:2"))
                .verifyComplete();
    }

    @Test
    void shouldRetrieveAllValuesInKeyspaceSuccessfully() {
        // assertions
        Set<String> keys = Set.of("keys:1", "keys:2");
        List<PaymentRequestInfo> values = keys.stream().map(
                key -> TransactionTestUtils.paymentRequestInfoV1()
        ).toList();
        Mockito.when(redisTemplate.keys("keys*")).thenReturn(Flux.fromIterable(keys));
        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        Mockito.when(valueOperations.multiGet(keys)).thenReturn(Mono.just(values));
        // test
        Flux<PaymentRequestInfo> returnedValues = paymentRequestInfoRedisTemplateWrapper.getAllValuesInKeySpace();

        // assertions
        StepVerifier.create(returnedValues)
                .expectNextSequence(values)
                .verifyComplete();

        Mockito.verify(redisTemplate, Mockito.times(1)).keys("keys*");
        Mockito.verify(valueOperations, Mockito.times(1)).multiGet(keys);
    }

    @Test
    void shouldWriteEventToStreamSuccessfully() {
        // assertions
        String streamKey = "streamKey";
        PaymentRequestInfo paymentRequestInfo = TransactionTestUtils.paymentRequestInfoV1();
        RecordId expectedRecordId = RecordId.of(System.currentTimeMillis(), 0);

        ReactiveStreamOperations<String, String, ObjectRecord<String, PaymentRequestInfo>> streamOps =
                Mockito.mock(ReactiveStreamOperations.class);

        Mockito.when(redisTemplate.opsForStream())
                .thenReturn((ReactiveStreamOperations) streamOps);

        Mockito.when(streamOps.add(ArgumentMatchers.<ObjectRecord<String, PaymentRequestInfo>>any()))
                .thenAnswer(inv -> {
                    ObjectRecord<String, PaymentRequestInfo> rec = inv.getArgument(0);
                    assertEquals(paymentRequestInfo, rec.getValue());
                    return Mono.just(expectedRecordId);
                });

        // act
        Mono<RecordId> result =
                paymentRequestInfoRedisTemplateWrapper.writeEventToStream(streamKey, paymentRequestInfo);

        // assert
        StepVerifier.create(result)
                .expectNext(expectedRecordId)
                .verifyComplete();

        Mockito.verify(streamOps, Mockito.times(1))
                .add(ArgumentMatchers.<ObjectRecord<String, PaymentRequestInfo>>any());
    }


    @Test
    void shouldWriteEventToStreamSuccessfullyTrimmingPreviousEvents() {
        // assertions
        String streamKey = "streamKey";
        int streamSize = 0;
        PaymentRequestInfo paymentRequestInfo = TransactionTestUtils.paymentRequestInfoV1();
        RecordId expectedRecordId = RecordId.of(System.currentTimeMillis(), 0);

        ReactiveStreamOperations<String, String, ObjectRecord<String, PaymentRequestInfo>> streamOps =
                Mockito.mock(ReactiveStreamOperations.class);
        Mockito.when(redisTemplate.opsForStream()).thenReturn((ReactiveStreamOperations) streamOps);

       // Mockito.when(redisTemplate.opsForStream()).thenReturn((ReactiveStreamOperations) streamOperations);
        Mockito.when(streamOperations.trim(streamKey, streamSize)).thenReturn(Mono.just(0L));

        Mockito.when(streamOps.add(ArgumentMatchers.<ObjectRecord<String, PaymentRequestInfo>>argThat(rec ->
                streamKey.equals(rec.getStream()) && paymentRequestInfo.equals(rec.getValue())
        ))).thenReturn(Mono.just(expectedRecordId));
        // test
        Mono<RecordId> result = paymentRequestInfoRedisTemplateWrapper.writeEventToStream(streamKey, paymentRequestInfo);

        StepVerifier.create(result)
                .expectNext(expectedRecordId)
                .verifyComplete();

        //Mockito.verify(streamOperations, Mockito.times(1)).add(any(ObjectRecord.class));
        Mockito.verify(streamOperations).trim(streamKey, streamSize);
        Mockito.verify(streamOperations).add(ArgumentMatchers.<ObjectRecord<String, PaymentRequestInfo>>any());
    }


    @Test
    void shouldTrimEventsSuccessfully() {
        // assertions
        String streamKey = "streamKey";
        int streamSize = 0;
        Mockito.when(redisTemplate.opsForStream()).thenReturn((ReactiveStreamOperations) streamOperations);
        Mockito.when(streamOperations.trim(streamKey, streamSize)).thenReturn(Mono.just(0L));
        // test
        paymentRequestInfoRedisTemplateWrapper
                .trimEvents(streamKey, streamSize).block();

        // assertions
        Mockito.verify(streamOperations, Mockito.times(1))
                .trim(streamKey, streamSize);
    }

    @Test
    void shouldThrowExceptionWritingEventToStreamWithInvalidStreamSize() {
        // assertions
        String streamKey = "streamKey";
        int streamSize = -1;
        PaymentRequestInfo paymentRequestInfo = TransactionTestUtils.paymentRequestInfoV1();
        RecordId expectedRecordId = RecordId.of(System.currentTimeMillis(), 0);

        Mockito.when(redisTemplate.opsForStream()).thenReturn((ReactiveStreamOperations) streamOperations);
        Mockito.when(streamOperations.trim(streamKey, streamSize)).thenReturn(Mono.just(0L));
        Mockito.when(streamOperations.add(argThat(r -> {
            ObjectRecord record = (ObjectRecord) r;
            return record.getValue().equals(paymentRequestInfo);
        }))).thenReturn(Mono.just(expectedRecordId));
        // test
        assertThrows(
                IllegalArgumentException.class,
                () -> paymentRequestInfoRedisTemplateWrapper
                        .writeEventToStreamTrimmingEvents(streamKey, paymentRequestInfo, streamSize).block()
        );

        // assertions
        Mockito.verify(streamOperations, Mockito.times(0))
                .add(any(ObjectRecord.class));
        Mockito.verify(streamOperations, Mockito.times(0))
                .trim(streamKey, streamSize);
    }

    @Test
    void shouldCreateStreamEventGroupSuccessfully() {
        // assertions
        String streamKey = "streamKey";
        String groupName = "groupName";
        Mockito.when(redisTemplate.opsForStream()).thenReturn((ReactiveStreamOperations) streamOperations);
        Mockito.when(streamOperations.createGroup(streamKey, groupName)).thenReturn(Mono.just("OK"));
        // test
        String outcome = paymentRequestInfoRedisTemplateWrapper.createGroup(streamKey, groupName).block();

        // assertions
        Mockito.verify(streamOperations, Mockito.times(1))
                .createGroup(streamKey, groupName);
        assertEquals("OK", outcome);
    }

    @Test
    void shouldCreateStreamEventGroupSuccessfullyWithCustomReadOffset() {
        // assertions
        String streamKey = "streamKey";
        String groupName = "groupName";
        ReadOffset offset = ReadOffset.from("0-0");
        Mockito.when(redisTemplate.opsForStream()).thenReturn((ReactiveStreamOperations) streamOperations);
        Mockito.when(streamOperations.createGroup(streamKey, offset, groupName)).thenReturn(Mono.just("OK"));
        // test
        String outcome = paymentRequestInfoRedisTemplateWrapper.createGroup(streamKey, groupName, offset).block();

        // assertions
        Mockito.verify(streamOperations, Mockito.times(1))
                .createGroup(streamKey, offset, groupName);
        assertEquals("OK", outcome);
    }

    @Test
    void shouldDestroyStreamEventGroupSuccessfully() {
        // assertions
        String streamKey = "streamKey";
        String groupName = "groupName";
        Mockito.when(redisTemplate.opsForStream()).thenReturn((ReactiveStreamOperations) streamOperations);
        Mockito.when(streamOperations.destroyGroup(streamKey, groupName)).thenReturn(Mono.just("OK"));
        // test
        Boolean outcome = paymentRequestInfoRedisTemplateWrapper.destroyGroup(streamKey, groupName).block();

        // assertions
        Mockito.verify(streamOperations, Mockito.times(1))
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
        Mockito.when(redisTemplate.opsForStream()).thenReturn((ReactiveStreamOperations) streamOperations);
        Mockito.when(streamOperations.acknowledge(streamKey, groupId, ids)).thenReturn(Mono.just(0L));
        // test
        paymentRequestInfoRedisTemplateWrapper
                .acknowledgeEvents(streamKey, groupId, ids).block();

        // assertions
        Mockito.verify(streamOperations, Mockito.times(1))
                .acknowledge(streamKey, groupId, ids);
    }

}
