package it.pagopa.ecommerce.commons.exceptions;

import it.pagopa.ecommerce.commons.client.NpgClient;
import org.springframework.http.HttpStatus;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

/**
 * Exception class wrapping checked exceptions that can occur during npg
 * invocation
 *
 * @see it.pagopa.ecommerce.commons.client.NpgClient
 */
public class NpgResponseException extends RuntimeException {
    /**
     * List of errors returned by NPG
     */
    private final List<NpgClient.GatewayError> errors;

    /**
     * HTTP status code related to the error
     */
    private final Optional<HttpStatus> statusCode;

    /**
     * Exception constructor for npg client with message and @see Throwable
     *
     * @param message    the error message
     * @param errors     the NPG error codes
     * @param statusCode the HTTP status code related to the error, if available
     * @param t          the throwable instance
     * @see RuntimeException
     */
    public NpgResponseException(
            @NotNull String message,
            @NotNull List<NpgClient.GatewayError> errors,
            @NotNull Optional<HttpStatus> statusCode,
            @NotNull Throwable t
    ) {
        super(message, t);
        this.errors = errors;
        this.statusCode = statusCode;
    }

    /**
     * Convenience constructor with empty error code list
     *
     * @param message    the error message
     * @param t          the throwable instance
     * @param statusCode the HTTP status code related to the error, if available
     * @see RuntimeException
     */
    public NpgResponseException(
            @NotNull String message,
            @NotNull Optional<HttpStatus> statusCode,
            @NotNull Throwable t
    ) {
        this(message, List.of(), statusCode, t);
    }

    /**
     * Errors getter
     *
     * @return error codes returned by NPG
     */
    @NotNull
    public List<NpgClient.GatewayError> getErrors() {
        return errors;
    }

    /**
     * HTTP status code getter
     *
     * @return the HTTP status code related to the exception
     */
    @NotNull
    public Optional<HttpStatus> getStatusCode() {
        return statusCode;
    }
}
