package it.pagopa.ecommerce.commons.utils;

import it.pagopa.ecommerce.commons.exceptions.UniqueIdGenerationException;
import it.pagopa.ecommerce.commons.redis.templatewrappers.UniqueIdTemplateWrapperReactive;
import it.pagopa.ecommerce.commons.repositories.UniqueIdDocument;
import reactor.core.publisher.Mono;

import java.security.SecureRandom;
import java.time.Duration;

/**
 * This class generate unique identifier
 */
public class UniqueIdUtils {
    private final UniqueIdTemplateWrapperReactive uniqueIdTemplateWrapper;
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final String ALPHANUMERICS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-._";
    private static final int MAX_LENGTH = 18;
    private static final int MAX_NUMBER_ATTEMPTS = 3;
    private static final String PRODUCT_PREFIX = "E";

    /**
     * Constructor
     *
     * @param uniqueIdTemplateWrapper redis template wrapper used for save id into
     *                                cache
     */
    public UniqueIdUtils(UniqueIdTemplateWrapperReactive uniqueIdTemplateWrapper) {
        this.uniqueIdTemplateWrapper = uniqueIdTemplateWrapper;
    }

    /**
     * This method generates a unique string and execute retry if the generated
     * string already exist into cache
     *
     * @return Mono with unique id value
     */
    public Mono<String> generateUniqueId() {
        return tryGenerateId(0, generateRandomIdentifier());
    }

    private Mono<String> tryGenerateId(
                                       int attempt,
                                       String uniqueId
    ) {
        if (attempt >= MAX_NUMBER_ATTEMPTS) {
            return Mono.error(new UniqueIdGenerationException());
        }

        UniqueIdDocument doc = new UniqueIdDocument(uniqueId);

        return uniqueIdTemplateWrapper
                .saveIfAbsent(doc, Duration.ofSeconds(60))
                .flatMap(success -> {
                    if (success) {
                        return Mono.just(uniqueId);
                    } else {
                        return tryGenerateId(attempt + 1, generateRandomIdentifier());
                    }
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
