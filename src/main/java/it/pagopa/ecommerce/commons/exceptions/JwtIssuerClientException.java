package it.pagopa.ecommerce.commons.exceptions;

/**
 * Exception thrown when an error occurs communicating with Node Forwarder
 */
public class JwtIssuerClientException extends RuntimeException {

    /**
     * Constructor
     *
     * @param reason the error reason
     * @param cause  the throwable error cause, if any
     */
    public JwtIssuerClientException(
            String reason,
            Throwable cause
    ) {
        super(reason, cause);
    }
}
