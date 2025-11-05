package it.pagopa.ecommerce.commons.redis.templatewrappers;

import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * This class is a {@link RedisTemplate} wrapper class, used to centralize
 * commons RedisTemplate operations
 *
 * @param <V> the RedisTemplate value type
 *
 * @deprecated Use
 *             {@link it.pagopa.ecommerce.commons.redis.reactivetemplatewrappers.ReactiveRedisTemplateWrapper}
 *             instead.
 */
public abstract class RedisTemplateWrapper<V> {

    private final RedisTemplate<String, V> redisTemplate;

    private final String keyspace;

    private final Duration ttl;

    /**
     * Primary constructor
     *
     * @param redisTemplate inner redis template
     * @param keyspace      keyspace associated to this wrapper
     * @param ttl           time to live for keys
     */
    protected RedisTemplateWrapper(
            @NonNull RedisTemplate<String, V> redisTemplate,
            @NonNull String keyspace,
            @NonNull Duration ttl
    ) {
        Objects.requireNonNull(redisTemplate, "RedisTemplate null not valid");
        Objects.requireNonNull(keyspace, "Keyspace null not valid");
        Objects.requireNonNull(ttl, "TTL null not valid");
        this.redisTemplate = redisTemplate;
        this.keyspace = keyspace;
        this.ttl = ttl;
    }

    /**
     * Save the input entity into Redis. The entity TTL will be set to the default
     * configured one
     *
     * @param value - the entity to be saved
     */
    public void save(V value) {
        save(value, getDefaultTTL());
    }

    /**
     * Save the input entity into Redis
     *
     * @param value the entity to be saved
     * @param ttl   the TTL for the entity to be saved. This parameter will override
     *              the default TTL value
     */
    public void save(
                     V value,
                     Duration ttl
    ) {
        redisTemplate.opsForValue().set(compoundKeyWithKeyspace(getKeyFromEntity(value)), value, ttl);
    }

    /**
     * Save key to hold the string value if key is absent (SET with NX).
     *
     * @param value the entity to be saved
     * @return returns false if it already exists, true if it does not exist.
     */
    public Boolean saveIfAbsent(
                                V value
    ) {
        return redisTemplate.opsForValue()
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
    public Boolean saveIfAbsent(
                                V value,
                                Duration ttl
    ) {
        return redisTemplate.opsForValue().setIfAbsent(compoundKeyWithKeyspace(getKeyFromEntity(value)), value, ttl);
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
    public Optional<V> findById(String key) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(compoundKeyWithKeyspace(key)));
    }

    /**
     * Delete the entity for the given key
     *
     * @param key - the entity key to be deleted
     * @return true if the key has been removed
     */
    public Boolean deleteById(String key) {
        return redisTemplate.delete(compoundKeyWithKeyspace(key));
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
    public Duration getTTL(String key) {
        Long duration = redisTemplate.getExpire(compoundKeyWithKeyspace(key));
        return duration != null ? Duration.ofSeconds(duration) : Duration.ofSeconds(-3);
    }

    /**
     * Write an event to the stream with the specified key
     *
     * @param streamKey the stream key where send the event to
     * @param event     the event to be sent
     * @return the {@link RecordId} associated to the written event
     */
    public RecordId writeEventToStream(
                                       String streamKey,
                                       V event
    ) {
        return redisTemplate
                .opsForStream()
                .add(
                        ObjectRecord.create(
                                streamKey,
                                event
                        )
                );
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
    public RecordId writeEventToStreamTrimmingEvents(
                                                     String streamKey,
                                                     V event,
                                                     long streamSize
    ) {
        if (streamSize < 0) {
            throw new IllegalArgumentException("Invalid input %s events to trim, it must be >=0".formatted(streamSize));
        }
        redisTemplate.opsForStream().trim(streamKey, streamSize);
        return redisTemplate
                .opsForStream()
                .add(
                        ObjectRecord.create(
                                streamKey,
                                event
                        )
                );
    }

    /**
     * Trim events from the stream with input key to the wanted size
     *
     * @param streamKey  the stream key from which trim events
     * @param streamSize the wanted stream size
     * @return the number or removed events from the stream
     */
    public Long trimEvents(
                           String streamKey,
                           long streamSize
    ) {
        return redisTemplate.opsForStream().trim(streamKey, streamSize);
    }

    /**
     * Acknowledge input record ids for group inside streamKey stream
     *
     * @param streamKey the stream key
     * @param groupId   the group id for which perform acknowledgment operation
     * @param recordIds records for which perform ack operation
     * @return the number of stream operations performed
     */
    public Long acknowledgeEvents(
                                  String streamKey,
                                  String groupId,
                                  String... recordIds
    ) {
        return redisTemplate.opsForStream().acknowledge(streamKey, groupId, recordIds);
    }

    /**
     * Create a consumer group positioned at the latest event offset for the stream
     * with input id
     *
     * @param streamKey the stream key for which create the group
     * @param groupName the group name
     * @return OK if operation was successful
     */
    public String createGroup(
                              String streamKey,
                              String groupName
    ) {
        return redisTemplate.opsForStream().createGroup(streamKey, groupName);
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
    public String createGroup(
                              String streamKey,
                              String groupName,
                              ReadOffset readOffset
    ) {
        return redisTemplate.opsForStream().createGroup(streamKey, readOffset, groupName);
    }

    /**
     * Destroy stream consumer group for the stream with input id
     *
     * @param streamKey the stream for which remove the group
     * @param groupName the group name to be destroyed
     * @return true iff the operation is completed successfully
     */
    public Boolean destroyGroup(
                                String streamKey,
                                String groupName
    ) {
        return redisTemplate.opsForStream().destroyGroup(streamKey, groupName);
    }

    /**
     * Get all the keys in keyspace
     *
     * @return a set populated with all the keys in keyspace
     */
    public Set<String> keysInKeyspace() {
        return redisTemplate.keys(keyspace.concat("*"));
    }

    /**
     * Get all the values in keyspace
     *
     * @return a list populated with all the entries in keyspace
     */
    public List<V> getAllValuesInKeySpace() {
        return redisTemplate.opsForValue().multiGet(keysInKeyspace());
    }

    /**
     * Unwrap this returning the underling used {@link RedisTemplate} instance
     *
     * @return this wrapper associated RedisTemplate instance
     */
    public RedisTemplate<String, V> unwrap() {
        return redisTemplate;
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
