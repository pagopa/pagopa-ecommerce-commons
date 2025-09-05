package it.pagopa.ecommerce.commons.redis.reactivetemplatewrappers;

import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;

/**
 * This class is a {@link ReactiveRedisTemplate} wrapper class, used to
 * centralize commons ReactiveRedisTemplate operations
 *
 * @param <V> - the ReactiveRedisTemplate value type
 */
public abstract class ReactiveRedisTemplateWrapper<V> {

    private final ReactiveRedisTemplate<String, V> reactiveRedisTemplate;

    private final String keyspace;

    private final Duration ttl;

    /**
     * Primary constructor
     *
     * @param reactiveRedisTemplate underlying reactive Redis template
     * @param keyspace              keyspace associated to this wrapper
     * @param ttl                   time to live for keys
     */
    protected ReactiveRedisTemplateWrapper(
            @NonNull ReactiveRedisTemplate<String, V> reactiveRedisTemplate,
            @NonNull String keyspace,
            @NonNull Duration ttl
    ) {
        Objects.requireNonNull(reactiveRedisTemplate, "ReactiveRedisTemplate null not valid");
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
     * @param value the entity to be saved
     * @return a {@link Mono} emitting {@code true} if the key was set,
     *         {@code false} otherwise
     */
    public Mono<Boolean> save(V value) {
        return save(value, getDefaultTTL());
    }

    /**
     * Save the input entity into Redis.
     *
     * @param value the entity to be saved
     * @param ttl   the TTL for the entity to be saved. This parameter overrides the
     *              default TTL value
     * @return a {@link Mono} emitting {@code true} if the key was set,
     *         {@code false} otherwise
     */
    public Mono<Boolean> save(
                              V value,
                              Duration ttl
    ) {
        return reactiveRedisTemplate.opsForValue().set(compoundKeyWithKeyspace(getKeyFromEntity(value)), value, ttl);
    }

    /**
     * Save key to hold the string value if key is absent (SET with NX).
     *
     * @param value the entity to be saved
     * @return a {@link Mono} emitting {@code true} if the key did not exist and was
     *         set, {@code false} otherwise
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
     * @return a {@link Mono} emitting {@code true} if the key did not exist and was
     *         set, {@code false} otherwise
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
     * @return a {@link Mono} emitting the value if present; empty if not found
     */
    public Mono<V> findById(String key) {
        return reactiveRedisTemplate.opsForValue().get(compoundKeyWithKeyspace(key));
    }

    /**
     * Delete the entity for the given key
     *
     * @param key - the entity key to be deleted
     * @return a {@link Mono} emitting {@code true} if a key was removed,
     *         {@code false} otherwise
     */
    public Mono<Boolean> deleteById(String key) {
        return reactiveRedisTemplate.delete(compoundKeyWithKeyspace(key)).map(deletedCount -> deletedCount > 0);
    }

    /**
     * Get TTL duration for the entity
     *
     * @param key - the entity key for which retrieve TTL
     * @return a {@link Mono} emitting the TTL {@link Duration}; may be empty if no
     *         expiration is set
     * @see org.springframework.data.redis.core.ReactiveRedisOperations#getExpire(Object)
     */
    public Mono<Duration> getTTL(String key) {
        return reactiveRedisTemplate
                .getExpire(compoundKeyWithKeyspace(key));
    }

    /**
     * Write an event to the stream with the specified key
     *
     * @param streamKey the stream key where send the event to
     * @param event     the event to be sent
     * @return a {@link Mono} emitting the {@link RecordId} of the written event
     */
    public Mono<RecordId> writeEventToStream(
                                             String streamKey,
                                             V event
    ) {
        return reactiveRedisTemplate
                .opsForStream()
                .add(ObjectRecord.create(streamKey, event));
    }

    /**
     * Write an event to the stream with the specified key trimming events before
     * writing the new events so that stream has the wanted size
     *
     * @param streamKey  the stream key where send the event to
     * @param event      the event to be sent
     * @param streamSize the wanted length of the stream
     * @return a {@link Mono} emitting the {@link RecordId} of the written event
     */
    public Mono<RecordId> writeEventToStreamTrimmingEvents(
                                                           String streamKey,
                                                           V event,
                                                           long streamSize
    ) {
        return Mono
                .just(streamSize)
                .filter(size -> size >= 0)
                .switchIfEmpty(
                        Mono.error(
                                new IllegalArgumentException(
                                        "Invalid input %s events to trim, it must be >=0".formatted(streamSize)
                                )
                        )
                )
                .flatMap(
                        size -> reactiveRedisTemplate
                                .opsForStream()
                                .trim(streamKey, size)
                                .then(
                                        reactiveRedisTemplate
                                                .opsForStream()
                                                .add(ObjectRecord.create(streamKey, event))
                                )
                );
    }

    /**
     * Trim events from the stream with input key to the wanted size
     *
     * @param streamKey  the stream key from which trim events
     * @param streamSize the wanted stream size
     * @return a {@link Mono} emitting the number of removed entries
     */
    public Mono<Long> trimEvents(
                                 String streamKey,
                                 long streamSize
    ) {
        return reactiveRedisTemplate
                .opsForStream()
                .trim(streamKey, streamSize);
    }

    /**
     * Acknowledge input record ids for group inside streamKey stream
     *
     * @param streamKey the stream key
     * @param groupId   the group id for which perform acknowledgment operation
     * @param recordIds records for which perform ack operation
     * @return a {@link Mono} emitting the number of acknowledged entries
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
     * @return a {@link Mono} emitting {@code "OK"} if the operation succeeded
     */
    public Mono<String> createGroup(
                                    String streamKey,
                                    String groupName
    ) {
        return reactiveRedisTemplate
                .opsForStream()
                .createGroup(streamKey, groupName);
    }

    /**
     * Create a consumer group positioned at the latest event offset for the stream
     * with input id
     *
     * @param streamKey  the stream key for which create the group
     * @param groupName  the group name
     * @param readOffset the offset from which start the receiver group
     * @return a {@link Mono} emitting {@code "OK"} if the operation succeeded
     */
    public Mono<String> createGroup(
                                    String streamKey,
                                    String groupName,
                                    ReadOffset readOffset
    ) {
        return reactiveRedisTemplate
                .opsForStream()
                .createGroup(streamKey, readOffset, groupName);
    }

    /**
     * Destroy stream consumer group for the stream with input id
     *
     * @param streamKey the stream for which remove the group
     * @param groupName the group name to be destroyed
     * @return a {@link Mono} emitting {@code true} if the group was destroyed;
     *         {@code false} otherwise
     */
    public Mono<Boolean> destroyGroup(
                                      String streamKey,
                                      String groupName
    ) {
        return reactiveRedisTemplate
                .opsForStream()
                .destroyGroup(streamKey, groupName)
                .map("OK"::equalsIgnoreCase)
                .defaultIfEmpty(false);
    }

    /**
     * Get all the keys in keyspace
     *
     * @return a {@link Flux} emitting all keys in the keyspace
     */
    public Flux<String> keysInKeyspace() {
        return reactiveRedisTemplate.keys(keyspace.concat("*"));
    }

    /**
     * Get all the values in keyspace
     *
     * @return a {@link Flux} emitting all values found in the keyspace
     */
    public Flux<V> getAllValuesInKeySpace() {
        return keysInKeyspace()
                .flatMap(this::findById);
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
