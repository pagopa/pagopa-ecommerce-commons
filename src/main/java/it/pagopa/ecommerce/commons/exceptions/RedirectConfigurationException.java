package it.pagopa.ecommerce.commons.exceptions;

/**
 * Exception thrown when NPG per PSP api key configuration cannot be
 * successfully parsed
 */
public class RedirectConfigurationException extends RuntimeException {

    /**
     * Constructor
     *
     * @param errorCause        error cause description
     * @param configurationType configuration type
     */
    public RedirectConfigurationException(
            String errorCause,
            RedirectConfigurationType configurationType
    ) {
        super(
                "Error parsing Redirect PSP %s configuration, cause: %s"
                        .formatted(configurationType, errorCause)
        );
    }
}
