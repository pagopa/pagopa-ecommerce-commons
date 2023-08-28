package it.pagopa.ecommerce.commons.exceptions;

import it.pagopa.ecommerce.commons.client.NpgClient;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Exception class wrapping checked exceptions that can occur during npg
 * invocation
 *
 * @see it.pagopa.ecommerce.commons.client.NpgClient
 */
public class NpgResponseException extends RuntimeException {
    private final List<NpgClient.GatewayError> errors;

    /**
     * Exception constructor for npg client with message and @see Throwable
     *
     * @param message the error message
     * @param errors  the NPG error codes
     * @param t       the throwable instance
     * @see RuntimeException
     */
    public NpgResponseException(
            @NotNull String message,
            @NotNull List<NpgClient.GatewayError> errors,
            @NotNull Throwable t
    ) {
        super(message, t);
        this.errors = errors;
    }

    /**
     * Convenience constructor with empty error code list
     *
     * @param message the error message
     * @param t       the throwable instance
     * @see RuntimeException
     */
    public NpgResponseException(
            @NotNull String message,
            @NotNull Throwable t
    ) {
        this(message, List.of(), t);
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
}
