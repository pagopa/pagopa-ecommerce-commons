package it.pagopa.ecommerce.commons.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception class wrapping checked exceptions that can occur during npg
 * invocation
 *
 * @see it.pagopa.ecommerce.commons.client.NpgClient
 */
public class NpgResponseException extends RuntimeException {

    /**
     * Http status code @see org.springframework.http.HttpStatus
     */
    public HttpStatus status;

    /**
     * Error reason
     */
    public String reason;

    /**
     * Exception constructor for npg client with status code and reason
     *
     * @param statusCode the http status code
     * @param reason     the error reason
     * @see it.pagopa.ecommerce.commons.client.NpgClient
     */
    public NpgResponseException(
            HttpStatus statusCode,
            String reason
    ) {
        this.reason = reason;
        this.status = statusCode;
    }
}
