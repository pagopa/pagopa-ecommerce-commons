package it.pagopa.ecommerce.commons.redis.v1.templatewrappers;

import it.pagopa.ecommerce.commons.redis.templatewrappers.v1.PaymentRequestInfoRedisTemplateWrapper;
import it.pagopa.ecommerce.commons.repositories.v1.PaymentRequestInfo;
import it.pagopa.ecommerce.commons.v1.TransactionTestUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;

class PaymentRequestInfoRedisTemplateWrapperTest {

    private final RedisTemplate<String, PaymentRequestInfo> redisTemplate = Mockito.mock(RedisTemplate.class);

    private final ValueOperations<String, PaymentRequestInfo> valueOperations = Mockito.mock(ValueOperations.class);

    private final StreamOperations<String, String, PaymentRequestInfo> streamOperations = Mockito
            .mock(StreamOperations.class);

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
        PaymentRequestInfo expected = TransactionTestUtils.paymentRequestInfoV1();
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
        PaymentRequestInfo paymentRequestInfo = TransactionTestUtils.paymentRequestInfoV1();
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
        PaymentRequestInfo paymentRequestInfo = TransactionTestUtils.paymentRequestInfoV1();
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
        PaymentRequestInfo paymentRequestInfo = TransactionTestUtils.paymentRequestInfoV1();
        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        Mockito.when(
                valueOperations.setIfAbsent("keys:%s".formatted(TransactionTestUtils.RPT_ID), paymentRequestInfo, ttl)
        ).thenReturn(true);
        // test
        paymentRequestInfoRedisTemplateWrapper.saveIfAbsent(paymentRequestInfo);
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
        ).thenReturn(true);
        // test
        paymentRequestInfoRedisTemplateWrapper.saveIfAbsent(paymentRequestInfo, customTTL);

        // assertions
        Mockito.verify(valueOperations, Mockito.times(1))
                .setIfAbsent("keys:%s".formatted(TransactionTestUtils.RPT_ID), paymentRequestInfo, customTTL);
    }

    @Test
    void shouldRetrieveAllKeysInKeyspaceSuccessfully() {
        // assertions
        Set<String> keys = Set.of("keys:1", "keys:2");
        Mockito.when(redisTemplate.keys("keys*")).thenReturn(keys);
        // test
        Set<String> returnedKeys = paymentRequestInfoRedisTemplateWrapper.keysInKeyspace();

        // assertions
        Mockito.verify(redisTemplate, Mockito.times(1))
                .keys("keys*");
        assertEquals(keys, returnedKeys);
    }

    @Test
    void shouldRetrieveAllValuesInKeyspaceSuccessfully() {
        // assertions
        Set<String> keys = Set.of("keys:1", "keys:2");
        List<PaymentRequestInfo> values = keys.stream().map(
                key -> TransactionTestUtils.paymentRequestInfoV1()
        ).toList();
        Mockito.when(redisTemplate.keys("keys*")).thenReturn(keys);
        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        Mockito.when(valueOperations.multiGet(keys)).thenReturn(values);
        // test
        List<PaymentRequestInfo> returnedValues = paymentRequestInfoRedisTemplateWrapper.getAllValuesInKeySpace();

        // assertions
        Mockito.verify(redisTemplate, Mockito.times(1))
                .keys("keys*");
        Mockito.verify(valueOperations, Mockito.times(1)).multiGet(keys);
        assertEquals(values, returnedValues);
    }

    @Test
    void shouldWriteEventToStreamSuccessfully() {
        // assertions
        String streamKey = "streamKey";
        PaymentRequestInfo paymentRequestInfo = TransactionTestUtils.paymentRequestInfoV1();
        Mockito.when(redisTemplate.opsForStream()).thenReturn((StreamOperations) streamOperations);

        Mockito.when(streamOperations.add(argThat(r -> {
            ObjectRecord record = (ObjectRecord) r;
            return record.getValue().equals(paymentRequestInfo);
        }))).thenReturn(RecordId.of(System.currentTimeMillis(), 0));
        // test
        paymentRequestInfoRedisTemplateWrapper.writeEventToStream(streamKey, paymentRequestInfo);

        // assertions
        Mockito.verify(streamOperations, Mockito.times(1))
                .add(any(ObjectRecord.class));
    }

    @Test
    void shouldWriteEventToStreamSuccessfullyTrimmingPreviousEvents() {
        // assertions
        String streamKey = "streamKey";
        int streamSize = 0;
        PaymentRequestInfo paymentRequestInfo = TransactionTestUtils.paymentRequestInfoV1();
        Mockito.when(redisTemplate.opsForStream()).thenReturn((StreamOperations) streamOperations);
        Mockito.when(streamOperations.trim(streamKey, streamSize)).thenReturn(0L);
        Mockito.when(streamOperations.add(argThat(r -> {
            ObjectRecord record = (ObjectRecord) r;
            return record.getValue().equals(paymentRequestInfo);
        }))).thenReturn(RecordId.of(System.currentTimeMillis(), 0));
        // test
        paymentRequestInfoRedisTemplateWrapper
                .writeEventToStreamTrimmingEvents(streamKey, paymentRequestInfo, streamSize);

        // assertions
        Mockito.verify(streamOperations, Mockito.times(1))
                .add(any(ObjectRecord.class));
        Mockito.verify(streamOperations, Mockito.times(1))
                .trim(streamKey, streamSize);
    }

    @Test
    void shouldTrimEventsSuccessfully() {
        // assertions
        String streamKey = "streamKey";
        int streamSize = 0;
        Mockito.when(redisTemplate.opsForStream()).thenReturn((StreamOperations) streamOperations);
        Mockito.when(streamOperations.trim(streamKey, streamSize)).thenReturn(0L);
        // test
        paymentRequestInfoRedisTemplateWrapper
                .trimEvents(streamKey, streamSize);

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
        Mockito.when(redisTemplate.opsForStream()).thenReturn((StreamOperations) streamOperations);
        Mockito.when(streamOperations.trim(streamKey, streamSize)).thenReturn(0L);
        Mockito.when(streamOperations.add(argThat(r -> {
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
        Mockito.when(redisTemplate.opsForStream()).thenReturn((StreamOperations) streamOperations);
        Mockito.when(streamOperations.createGroup(streamKey, groupName)).thenReturn("OK");
        // test
        String outcome = paymentRequestInfoRedisTemplateWrapper.createGroup(streamKey, groupName);

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
        Mockito.when(redisTemplate.opsForStream()).thenReturn((StreamOperations) streamOperations);
        Mockito.when(streamOperations.createGroup(streamKey, offset, groupName)).thenReturn("OK");
        // test
        String outcome = paymentRequestInfoRedisTemplateWrapper.createGroup(streamKey, groupName, offset);

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
        Mockito.when(redisTemplate.opsForStream()).thenReturn((StreamOperations) streamOperations);
        Mockito.when(streamOperations.destroyGroup(streamKey, groupName)).thenReturn(Boolean.TRUE);
        // test
        Boolean outcome = paymentRequestInfoRedisTemplateWrapper.destroyGroup(streamKey, groupName);

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
        Mockito.when(redisTemplate.opsForStream()).thenReturn((StreamOperations) streamOperations);
        Mockito.when(streamOperations.acknowledge(streamKey, groupId, ids)).thenReturn(0L);
        // test
        paymentRequestInfoRedisTemplateWrapper
                .acknowledgeEvents(streamKey, groupId, ids);

        // assertions
        Mockito.verify(streamOperations, Mockito.times(1))
                .acknowledge(streamKey, groupId, ids);
    }

}
