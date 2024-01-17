package it.pagopa.ecommerce.commons.exceptions;

import it.pagopa.ecommerce.commons.utils.ConfidentialDataManager;
import org.springframework.http.HttpStatus;

import javax.validation.constraints.NotNull;
import java.util.Optional;

/**
 * Exception class wrapping checked exceptions that can occur during encryption
 * or decryption of confidential data
 *
 * @see ConfidentialDataManager
 */
public class ConfidentialDataException extends RuntimeException {

    /**
     * HTTP status code related to the error for WebClientResponseException
     */
    private final Optional<HttpStatus> statusCode;

    /**
     * Primary exception constructor
     *
     * @param throwable  the exception to be wrapped
     * @param httpStatus the http status saved in case of WebClientResponseException
     */
    public ConfidentialDataException(
            @NotNull Throwable throwable,
            Optional<HttpStatus> httpStatus
    ) {
        super("Exception during confidential data encrypt/decrypt", throwable);
        this.statusCode = httpStatus;
    }
}
