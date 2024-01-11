package it.pagopa.ecommerce.commons.exceptions;

/**
 * Exception thrown when NPG per PSP api key configuration cannot be
 * successfully parsed
 */
public class CheckoutRedirectConfigurationException extends RuntimeException {

    /**
     * Constructor
     *
     * @param errorCause error cause description
     */
    public CheckoutRedirectConfigurationException(
            String errorCause
    ) {
        super(
                "Error parsing Checkout Redirect PSP api keys configuration, cause: %s"
                        .formatted(errorCause)
        );
    }
}
