package it.pagopa.ecommerce.commons.utils;

import it.pagopa.ecommerce.commons.exceptions.UniqueIdGenerationException;
import it.pagopa.ecommerce.commons.redis.reactivetemplatewrappers.ReactiveUniqueIdTemplateWrapper;
import it.pagopa.ecommerce.commons.redis.templatewrappers.UniqueIdTemplateWrapper;
import it.pagopa.ecommerce.commons.repositories.UniqueIdDocument;
import reactor.core.publisher.Mono;

import java.security.SecureRandom;
import java.time.Duration;

/**
 * This class generate unique identifier
 */
public class ReactiveUniqueIdUtils {
    private final ReactiveUniqueIdTemplateWrapper reactiveUniqueIdTemplateWrapper;
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final String ALPHANUMERICS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-._";
    private static final int MAX_LENGTH = 18;
    private static final int MAX_NUMBER_ATTEMPTS = 3;
    private static final String PRODUCT_PREFIX = "E";

    /**
     * Constructor
     *
     * @param reactiveUniqueIdTemplateWrapper reactive redis template wrapper used for save id into
     *                                cache
     */
    public ReactiveUniqueIdUtils(ReactiveUniqueIdTemplateWrapper reactiveUniqueIdTemplateWrapper) {
        this.reactiveUniqueIdTemplateWrapper = reactiveUniqueIdTemplateWrapper;
    }

//    /**
//     * This method generates a unique string and execute retry if the generated
//     * string already exist into cache
//     *
//     * @return Mono with unique id value
//     */
//    public Mono<String> generateUniqueId() {
//        boolean isSuccessfullySaved = false;
//        int attempt = 0;
//        String uniqueId = generateRandomIdentifier();
//        while (attempt < MAX_NUMBER_ATTEMPTS && !isSuccessfullySaved) {
//            isSuccessfullySaved = reactiveUniqueIdTemplateWrapper
//                    .saveIfAbsent(new UniqueIdDocument(uniqueId), Duration.ofSeconds(60));
//            attempt++;
//            if (!isSuccessfullySaved) {
//                uniqueId = generateRandomIdentifier();
//            }
//        }
//        return !isSuccessfullySaved ? Mono.error(new UniqueIdGenerationException()) : Mono.just(uniqueId);
//    }

    /**
     * Generate a unique identifier stored in Redis with a limited TTL.
     *
     * @return a {@link Mono} that emits the generated unique identifier string,
     *         or an error if the generation failed after all retries.
     */
    public Mono<String> generateUniqueId() {
        return attemptGenerate(MAX_NUMBER_ATTEMPTS);
    }

    /**
     * Recursive helper method to attempt generation of a unique identifier.
     *
     * @param remainingAttempts the number of attempts left
     * @return a {@link Mono} that emits a unique identifier if successfully saved
     *         in Redis, retries otherwise, or errors when attempts are exhausted
     */
    private Mono<String> attemptGenerate(int remainingAttempts) {
        String uniqueId = generateRandomIdentifier();
        return reactiveUniqueIdTemplateWrapper
                .saveIfAbsent(new UniqueIdDocument(uniqueId), Duration.ofSeconds(60))
                .flatMap(saved -> {
                    if (saved) {
                        return Mono.just(uniqueId);
                    }
                    if (remainingAttempts > 1) {
                        return attemptGenerate(remainingAttempts - 1);
                    }
                    return Mono.error(new UniqueIdGenerationException());
                });
    }


    /**
     * This method generate a unique string by concatenating the millisecond
     * timestamp with a random suffix
     *
     * @return generated string
     */
    private static String generateRandomIdentifier() {
        StringBuilder uniqueId = new StringBuilder(PRODUCT_PREFIX);
        uniqueId.append(System.currentTimeMillis());
        int randomStringLength = MAX_LENGTH - uniqueId.length();
        return uniqueId.append(generateRandomString(randomStringLength)).toString();
    }

    /**
     * This method generates a unique string of length n by choosing from a list of
     * alphanumeric characters
     *
     * @param length length of string to be generated
     * @return generated suffix
     */
    private static String generateRandomString(int length) {
        StringBuilder stringBuilder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            stringBuilder.append(ALPHANUMERICS.charAt(secureRandom.nextInt(ALPHANUMERICS.length())));
        }
        return stringBuilder.toString();
    }

}
