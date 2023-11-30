package it.pagopa.ecommerce.commons.exceptions;

/**
 * Exception class wrapping checked exceptions that can occur during unique id
 * generation
 *
 * @see it.pagopa.ecommerce.commons.client.NpgClient
 */
public class UniqueIdGenerationException extends RuntimeException {

    /**
     * Exception constructor for unique id generation with message and @see
     * Throwable
     *
     * @see RuntimeException
     */
    public UniqueIdGenerationException() {
        super(
                "Error when generating unique id"
        );
    }
}
