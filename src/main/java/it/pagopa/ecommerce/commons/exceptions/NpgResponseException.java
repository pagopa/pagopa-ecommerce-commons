package it.pagopa.ecommerce.commons.exceptions;

import org.springframework.http.HttpStatus;

public class NpgResponseException extends RuntimeException {
    public HttpStatus status;
    public String reason;

    public NpgResponseException(
            HttpStatus statusCode,
            String reason
    ) {
        this.reason = reason;
        this.status = statusCode;
    }
}
