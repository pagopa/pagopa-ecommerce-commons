package it.pagopa.ecommerce.commons.exceptions;

/**
 * Exception class wrapping checked exceptions that can occur during encryption
 * or decryption of confidential data
 *
 * @see it.pagopa.ecommerce.commons.utils.v1.ConfidentialDataManager or
 * @see it.pagopa.ecommerce.commons.utils.v2.ConfidentialDataManager
 *
 */
public class ConfidentialDataException extends RuntimeException {

    /**
     * Primary exception constructor
     *
     * @param e the exception to be wrapped
     */
    public ConfidentialDataException(Exception e) {
        super("Exception during confidential data encrypt/decrypt", e);
    }
}
