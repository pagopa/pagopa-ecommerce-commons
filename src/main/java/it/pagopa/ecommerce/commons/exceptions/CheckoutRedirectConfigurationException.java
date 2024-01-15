package it.pagopa.ecommerce.commons.exceptions;

/**
 * Exception thrown when NPG per PSP api key configuration cannot be
 * successfully parsed
 */
public class CheckoutRedirectConfigurationException extends RuntimeException {

    /**
     * Constructor
     *
     * @param errorCause        error cause description
     * @param configurationType configuration type
     */
    public CheckoutRedirectConfigurationException(
            String errorCause,
            CheckoutRedirectConfigurationType configurationType
    ) {
        super(
                "Error parsing Checkout Redirect PSP %s configuration, cause: %s"
                        .formatted(configurationType, errorCause)
        );
    }
}
