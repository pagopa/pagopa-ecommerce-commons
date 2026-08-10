package it.pagopa.ecommerce.commons.exceptions;

/**
 * Exception thrown when an error occurs during the execution of logging or
 * tracing utilities.
 * <p>
 * This runtime exception is typically used within the logging utilities (e.g.,
 * {@code LogTracingUtils}) to indicate invalid states, missing mandatory
 * configurations, or unexpected behaviors during log building and context
 * enrichment.
 * </p>
 */
public class LogTracingUtilException extends RuntimeException {

    /**
     * Constructs a new {@code LogTracingUtilException} with the specified detail
     * message.
     *
     * @param message the detail message explaining the reason for the exception,
     *                which is saved for later retrieval by the
     *                {@link Throwable#getMessage()} method
     */
    public LogTracingUtilException(String message) {
        super(message);
    }
}
