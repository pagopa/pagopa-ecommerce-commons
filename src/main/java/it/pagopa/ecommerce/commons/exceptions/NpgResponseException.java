package it.pagopa.ecommerce.commons.exceptions;

/**
 * Exception class wrapping checked exceptions that can occur during npg
 * invocation
 *
 * @see it.pagopa.ecommerce.commons.client.NpgClient
 */
public class NpgResponseException extends RuntimeException {

    /**
     * Exception constructor for npg client with status code and reason
     *
     * @param message the error message
     * @see RuntimeException
     */
    public NpgResponseException(String message) {
        super(message);
    }

    public NpgResponseException(
            String message,
            Throwable t
    ) {
        super(message, t);
    }
}
