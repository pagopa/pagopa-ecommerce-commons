package it.pagopa.ecommerce.commons.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Least recently used cache implementation. This cache leave in memory at most
 * the configured maxItems number of entries. When add a new entry and the
 * maxItems number if reached the least recently used record is removed leaving
 * in cache only the records that have been used most recently. This check if
 * performed associating to each entry in map the last hit timestamp that is the
 * last timestamp when this entry has been retrieved. This cache is internally
 * synchronized using a {@link ReentrantReadWriteLock} so that multiple reads
 * can be performed concurrently locking in case of a write operations.
 * Concurrent write operations will be serialized allowing for concurrent writes
 * too
 *
 * @param <K> the mapping key type
 * @param <V> the map value
 */
public class LRUCache<K, V> {

    private final Map<K, LruCacheEntry<V>> internalCache = new HashMap<>();
    private final long maxItems;

    /**
     * Reentrant read write lock used to synchronize concurrent cache access
     */
    private final ReadWriteLock lock;

    record LruCacheEntry<V> (
            V value,
            long lastHitTimestamp
    ) {
        LruCacheEntry(V value) {
            this(value, System.currentTimeMillis());
        }
    }

    /**
     * Constructor
     *
     * @param maxItems number of max allowed records into cache, must be a positive
     *                 integer
     * @throws IllegalArgumentException for 0 or negative maxItems input value
     */
    public LRUCache(long maxItems) {
        this(maxItems, new ReentrantReadWriteLock());
    }

    LRUCache(
            long maxItems,
            ReadWriteLock lock
    ) {
        if (maxItems <= 0) {
            throw new IllegalArgumentException(
                    "Invalid maxItems: [%s], maxItems parameter must be >0".formatted(maxItems)
            );
        }
        this.maxItems = maxItems;
        this.lock = lock;
    }

    /**
     * Add a new value into the cache, removing the least recently used one's if
     * maxItems limit has been reached
     *
     * @param key   the key to associate to the element
     * @param value the value to add to the cache
     */
    public void put(
                    K key,
                    V value
    ) {
        // acquire write lock
        lock.writeLock().lock();
        try {
            // the internal cache doesn't contain the input key and the max items limit has
            // been reached -> proceed to delete least recently used entry to leave space
            // for the new entry
            if (!internalCache.containsKey(key) && internalCache.size() == maxItems) {
                removeLeastRecentlyUsedEntry();
            }
            internalCache.put(key, new LruCacheEntry<>(value));
        } finally {
            // release write lock
            lock.writeLock().unlock();
        }
    }

    /**
     * Retrieve the value associated to the input key
     *
     * @param key the key to be searched for
     * @return an optional containing the associated value to the key
     */
    public Optional<V> get(K key) {
        // acquire write lock since each cache hit will write to cache table in order to
        // update last hit timestamp
        lock.writeLock().lock();
        try {
            LruCacheEntry<V> value = internalCache.get(key);
            return Optional
                    .ofNullable(value)
                    .map(lruEntry -> {
                        // there is a cache hit, update the last hit timestamp associated to the entry
                        internalCache.put(key, new LruCacheEntry<>(lruEntry.value));
                        return lruEntry.value;

                    });
        } finally {
            // release write lock
            lock.writeLock().unlock();
        }
    }

    /**
     * Returns the current internal cache map view
     *
     * @return an unmodifiable map containing all cache mappings
     */
    public Map<K, V> getInternalCacheMap() {
        this.lock.readLock().lock();
        try {
            Map<K, V> mappings = new HashMap<>();
            internalCache.forEach(
                    (
                     k,
                     v
                    ) -> mappings.put(k, v.value())
            );
            return Collections.unmodifiableMap(mappings);
        } finally {
            this.lock.readLock().unlock();
        }
    }

    /**
     * This method will search for the entry that has least recently used and remove
     * it from the internal cache
     */
    private void removeLeastRecentlyUsedEntry() {
        K keyToRemove = null;
        long oldHitTimestamp = Long.MAX_VALUE;
        long currentHitTimestamp;
        for (Map.Entry<K, LruCacheEntry<V>> entry : internalCache.entrySet()) {
            currentHitTimestamp = entry.getValue().lastHitTimestamp;
            if (oldHitTimestamp > currentHitTimestamp) {
                oldHitTimestamp = currentHitTimestamp;
                keyToRemove = entry.getKey();
            }
        }
        if (keyToRemove != null) {
            internalCache.remove(keyToRemove);
        }
    }

}
