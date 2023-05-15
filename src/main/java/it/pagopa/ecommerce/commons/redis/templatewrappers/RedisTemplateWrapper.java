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
abstract sealed class RedisTemplateWrapper<V> permits PaymentRequestInfoRedisTemplateWrapper {

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
     * Save the input entity into Redis
     *
     * @param value - the entity to be saved
     */
    public void save(V value) {
        redisTemplate.opsForValue().set(compoundKeyWithKeyspace(getKeyFromEntity(value)), value, ttl);
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
     * Delete the entity for the given key and return it
     *
     * @param key - the entity key to be deleted
     * @return the deleted key
     */
    public Boolean deleteById(String key) {
        return redisTemplate.delete(compoundKeyWithKeyspace(key));
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
