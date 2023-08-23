package it.pagopa.ecommerce.commons.redis.templatewrappers;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

/**
 * This class is a {@link RedisTemplate} wrapper class, used to centralize
 * commons RedisTemplate operations
 *
 * @param <V> - the RedisTemplate value type
 */
abstract class RedisTemplateWrapper<V> {

    private final RedisTemplate<String, V> redisTemplate;

    private final String keyspace;

    private final Duration ttl;

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
