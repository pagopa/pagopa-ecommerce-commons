package it.pagopa.ecommerce.commons.exceptions;

import javax.validation.constraints.NotNull;

/**
 * Exception class wrapping checked exceptions that can occur during npg
 * invocation
 *
 * @see it.pagopa.ecommerce.commons.client.NpgClient
 */
public class NpgResponseException extends RuntimeException {
    /**
     * Exception constructor for npg client with message and @see Throwable
     *
     * @param message the error message
     * @param t       the throwable instance
     * @see RuntimeException
     */
    public NpgResponseException(
            @NotNull String message,
            @NotNull Throwable t
    ) {
        super(message, t);
    }
}
