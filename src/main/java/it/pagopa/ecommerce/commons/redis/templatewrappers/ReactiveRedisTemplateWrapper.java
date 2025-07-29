package it.pagopa.ecommerce.commons.redis.templatewrappers;

import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;

/**
 * This class is a {@link RedisTemplate} wrapper class, used to centralize
 * commons RedisTemplate operations
 *
 * @param <V> - the RedisTemplate value type
 */
public abstract class ReactiveRedisTemplateWrapper<V> {

    private final ReactiveRedisTemplate<String, V> reactiveRedisTemplate;

    private final String keyspace;

    private final Duration ttl;

    /**
     * Primary constructor
     *
     * @param reactiveRedisTemplate inner redis template
     * @param keyspace              keyspace associated to this wrapper
     * @param ttl                   time to live for keys
     */
    protected ReactiveRedisTemplateWrapper(
            @NonNull ReactiveRedisTemplate<String, V> reactiveRedisTemplate,
            @NonNull String keyspace,
            @NonNull Duration ttl
    ) {
        Objects.requireNonNull(reactiveRedisTemplate, "RedisTemplate null not valid");
        Objects.requireNonNull(keyspace, "Keyspace null not valid");
        Objects.requireNonNull(ttl, "TTL null not valid");
        this.reactiveRedisTemplate = reactiveRedisTemplate;
        this.keyspace = keyspace;
        this.ttl = ttl;
    }

    /**
     * Save the input entity into Redis. The entity TTL will be set to the default
     * configured one
     *
     * @param value - the entity to be saved
     */
    public Mono<Boolean> save(V value) {
        return save(value, ttl);
    }

    /**
     * Save the input entity into Redis
     *
     * @param value the entity to be saved
     * @param ttl   the TTL for the entity to be saved. This parameter will override
     *              the default TTL value
     */
    public Mono<Boolean> save(
                              V value,
                              Duration ttl
    ) {
        String key = keyspace + ":" + getKeyFromEntity(value);
        return reactiveRedisTemplate.opsForValue().set(key, value, ttl);
    }

    /**
     * Save key to hold the string value if key is absent (SET with NX).
     *
     * @param value the entity to be saved
     * @return returns false if it already exists, true if it does not exist.
     */
    public Mono<Boolean> saveIfAbsent(
                                      V value
    ) {
        return reactiveRedisTemplate.opsForValue()
                .setIfAbsent(compoundKeyWithKeyspace(getKeyFromEntity(value)), value, getDefaultTTL());
    }

    /**
     * Save key to hold the string value if key is absent (SET with NX).
     *
     * @param value the entity to be saved
     * @param ttl   the TTL for the entity to be saved. This parameter will override
     *              the default TTL value
     * @return returns false if it already exists, true if it does not exist.
     */
    public Mono<Boolean> saveIfAbsent(
                                      V value,
                                      Duration ttl
    ) {
        return reactiveRedisTemplate.opsForValue()
                .setIfAbsent(compoundKeyWithKeyspace(getKeyFromEntity(value)), value, ttl);
    }

    /**
     * Get the default configured TTL
     *
     * @return the default configured TTL
     */
    public Duration getDefaultTTL() {
        return this.ttl;
    }

    /**
     * Retrieve entity for the given key
     *
     * @param key - the key of the entity to be found
     * @return an Optional object valued with the found entity, if any
     */
    public Mono<V> findById(String key) {
        String fullKey = compoundKeyWithKeyspace(key);
        return reactiveRedisTemplate.opsForValue().get(fullKey);
    }

    /**
     * Delete the entity for the given key
     *
     * @param key - the entity key to be deleted
     * @return true if the key has been removed
     */
    public Mono<Boolean> deleteById(String key) {
        String fullKey = compoundKeyWithKeyspace(key);
        return reactiveRedisTemplate
                .delete(fullKey)
                .map(deletedCount -> deletedCount > 0);
    }

    /**
     * Get TTL duration for the entity with the given key Negative duration has the
     * following meaning:
     * <ul>
     * <li>-1: entity that has no expiration set</li>
     * <li>-2: input key does not exist</li>
     * <li>-3: redis returned expiration is null</li>
     * </ul>
     *
     * @param key - the entity key for which retrieve TTL
     * @return the entity associated TTL duration.
     */
    public Mono<Duration> getTTL(String key) {
        String fullKey = compoundKeyWithKeyspace(key);
        return reactiveRedisTemplate
                .getExpire(fullKey)
                .defaultIfEmpty(Duration.ofSeconds(-3));
    }

    /**
     * Write an event to the stream with the specified key
     *
     * @param streamKey the stream key where send the event to
     * @param event     the event to be sent
     * @return the {@link RecordId} associated to the written event
     */
    public Mono<RecordId> writeEventToStream(
                                             String streamKey,
                                             V event
    ) {
        ObjectRecord<String, V> record = ObjectRecord.create(streamKey, event);
        return reactiveRedisTemplate
                .opsForStream()
                .add(record);
    }

    /**
     * Write an event to the stream with the specified key trimming events before
     * writing the new events so that stream has the wanted size
     *
     * @param streamKey  the stream key where send the event to
     * @param event      the event to be sent
     * @param streamSize the wanted length of the stream
     * @return the {@link RecordId} associated to the written event
     */
    public Mono<RecordId> writeEventToStreamTrimmingEvents(
                                                           String streamKey,
                                                           V event,
                                                           long streamSize
    ) {
        if (streamSize < 0) {
            return Mono.error(
                    new IllegalArgumentException(
                            String.format("Invalid input %s events to trim, it must be >=0", streamSize)
                    )
            );
        }

        ObjectRecord<String, V> record = ObjectRecord.create(streamKey, event);

        return reactiveRedisTemplate
                .opsForStream()
                .trim(streamKey, streamSize)
                .then(reactiveRedisTemplate.opsForStream().add(record));
    }

    /**
     * Trim events from the stream with input key to the wanted size
     *
     * @param streamKey  the stream key from which trim events
     * @param streamSize the wanted stream size
     * @return the number or removed events from the stream
     */
    public Mono<Long> trimEvents(
                                 String streamKey,
                                 long streamSize
    ) {
        return reactiveRedisTemplate.opsForStream().trim(streamKey, streamSize);
    }

    /**
     * Acknowledge input record ids for group inside streamKey stream
     *
     * @param streamKey the stream key
     * @param groupId   the group id for which perform acknowledgment operation
     * @param recordIds records for which perform ack operation
     * @return the number of stream operations performed
     */
    public Mono<Long> acknowledgeEvents(
                                        String streamKey,
                                        String groupId,
                                        String... recordIds
    ) {
        return reactiveRedisTemplate
                .opsForStream()
                .acknowledge(streamKey, groupId, recordIds);
    }

    /**
     * Create a consumer group positioned at the latest event offset for the stream
     * with input id
     *
     * @param streamKey the stream key for which create the group
     * @param groupName the group name
     * @return OK if operation was successful
     */
    public Mono<String> createGroup(
                                    String streamKey,
                                    String groupName
    ) {
        return reactiveRedisTemplate
                .opsForStream()
                .createGroup(streamKey, groupName)
                .thenReturn("OK");
    }

    /**
     * Create a consumer group positioned at the latest event offset for the stream
     * with input id
     *
     * @param streamKey  the stream key for which create the group
     * @param groupName  the group name
     * @param readOffset the offset from which start the receiver group
     * @return OK if operation was successful
     */
    public Mono<String> createGroup(
                                    String streamKey,
                                    String groupName,
                                    ReadOffset readOffset
    ) {
        return reactiveRedisTemplate
                .opsForStream()
                .createGroup(streamKey, readOffset, groupName)
                .thenReturn("OK");
    }

    /**
     * Destroy stream consumer group for the stream with input id
     *
     * @param streamKey the stream for which remove the group
     * @param groupName the group name to be destroyed
     * @return true iff the operation is completed successfully
     */
    public Mono<Boolean> destroyGroup(
                                      String streamKey,
                                      String groupName
    ) {
        return reactiveRedisTemplate
                .opsForStream()
                .destroyGroup(streamKey, groupName)
                .map("OK"::equalsIgnoreCase)
                .onErrorReturn(false);
    }

    /**
     * Get all the keys in keyspace
     *
     * @return a set populated with all the keys in keyspace
     */
    public Flux<String> keysInKeyspace() {
        String fullKey = compoundKeyWithKeyspace("*");
        return reactiveRedisTemplate
                .keys(fullKey);
    }

    /**
     * Get all the values in keyspace
     *
     * @return a list populated with all the entries in keyspace
     */
    public Flux<V> allValuesInKeySpace() {
        return reactiveRedisTemplate
                .keys(keyspace + "*")
                .collectList()
                .flatMapMany(keys -> {
                    if (keys.isEmpty()) {
                        return Flux.empty();
                    }
                    return reactiveRedisTemplate
                            .opsForValue()
                            .multiGet(new HashSet<>(keys))
                            .flatMapMany(
                                    values -> Flux.fromIterable(
                                            values.stream()
                                                    .filter(Objects::nonNull)
                                                    .toList()
                                    )
                            );
                });
    }

    /**
     * Unwrap this returning the underling used {@link RedisTemplate} instance
     *
     * @return this wrapper associated RedisTemplate instance
     */
    public ReactiveRedisTemplate<String, V> unwrap() {
        return reactiveRedisTemplate;
    }

    /**
     * Get the Redis key from the input entity
     *
     * @param value - the entity value from which retrieve the Redis key
     * @return the key associated to the input entity
     */
    protected abstract String getKeyFromEntity(V value);

    private String compoundKeyWithKeyspace(String key) {
        return "%s:%s".formatted(keyspace, key);
    }
}
