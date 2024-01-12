package it.pagopa.ecommerce.commons.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class LRUCacheTest {

    private final int cacheMaxItems = 5;

    private final ReadWriteLock lock = Mockito.spy(new ReentrantReadWriteLock());
    private final LRUCache<String, String> lruCache = new LRUCache<>(cacheMaxItems, lock);

    @Test
    void shouldAddAllElementsToCacheRemovingLeastRecentlyUsedOnes() {
        // pre-conditions
        List<String> elements = IntStream.range(0, 10).mapToObj(String::valueOf).toList();
        Mono<Void> testMono = Flux
                .fromIterable(elements)
                // delay each element of 100 millis in order to have different last update
                // timestamps
                .delayElements(Duration.ofMillis(100))
                .doOnNext(element -> {
                    /*
                     * Here we retrieve always the first two element in order to simulate a cache
                     * hit for the first two and so that this two elements will be present into the
                     * final cache (because their last hit timestamp will be updated before any new
                     * element insertion into cache
                     */
                    lruCache.get("1");
                    lruCache.get("2");
                    lruCache.put(element, element);
                }).collectList().then();
        // test
        StepVerifier
                .create(
                        testMono
                ).expectNext()
                .verifyComplete();
        // assertions
        Map<String, String> cacheView = lruCache.getInternalCacheMap();
        // here we expected that LRU cache contains the 1 and 2 keys (the ones for which
        // we trigger a cache hit during test before any insertion and the last added
        // ones
        Set<String> expectedKeys = Set.of("1", "2", "7", "8", "9");
        assertEquals(cacheMaxItems, cacheView.size());
        assertTrue(cacheView.keySet().containsAll(expectedKeys));

    }

    @Test
    void shouldAddAllElementsHandlingConcurrentMapAccess() {
        // pre-conditions
        int concurrentItemsToAdd = 10000;
        List<String> elements = IntStream.range(0, concurrentItemsToAdd).mapToObj(String::valueOf).toList();
        Mono<Void> testMono = Flux
                .fromIterable(elements)
                .parallel(concurrentItemsToAdd)
                .runOn(Schedulers.parallel())
                .doOnNext(element -> lruCache.put(element, element))
                .then();
        // test
        StepVerifier
                .create(
                        testMono
                ).expectNext()
                .verifyComplete();
        // add a new element to cache that has to be present deleting the internal least
        // recently added ones
        lruCache.put("1", "1");
        // assertions
        Map<String, String> cacheView = lruCache.getInternalCacheMap();
        assertEquals(cacheMaxItems, cacheView.size());
        assertTrue(cacheView.containsKey("1"));
    }

    @Test
    void shouldRetrieveValuesFromCacheSuccessfully() {
        // pre-conditions
        lruCache.put("1", "1");
        // assertions
        Optional<String> cacheHitValue = lruCache.get("1");
        Optional<String> missingKey = lruCache.get("2");
        assertTrue(cacheHitValue.isPresent());
        assertEquals("1", cacheHitValue.get());
        assertTrue(missingKey.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(
            ints = {
                    0,
                    Integer.MIN_VALUE
            }
    )
    void shouldThrowExceptionForInvalidMaxItemsValue(int invalidMaxItems) {
        assertThrows(IllegalArgumentException.class, () -> new LRUCache<>(invalidMaxItems));
    }

}
