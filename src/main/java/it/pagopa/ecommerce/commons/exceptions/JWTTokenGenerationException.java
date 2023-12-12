package it.pagopa.ecommerce.commons.exceptions;

/**
 * Exception class wrapping checked exceptions that can occur during jwt
 * generation
 *
 * @see it.pagopa.ecommerce.commons.utils.JwtTokenUtils
 */
public class JWTTokenGenerationException extends RuntimeException {

    /**
     * Exception constructor for jwt generation with message and @see Throwable
     *
     * @see RuntimeException
     */
    public JWTTokenGenerationException() {
        super("JWT token generation error");
    }
}
