package it.pagopa.ecommerce.commons.exceptions;

import java.util.Set;

/**
 * Exception thrown when requesting an API key from NPG configuration for a
 * nonexisting PSP
 */
public class CheckoutRedirectMissingPspRequestedException extends RuntimeException {

    /**
     * Constructor
     *
     * @param psp               requested PSP
     * @param availablePsps     currently configured PSPs
     * @param configurationType configuration type
     */
    public CheckoutRedirectMissingPspRequestedException(
            String psp,
            Set<String> availablePsps,
            CheckoutRedirectConfigurationType configurationType
    ) {
        super(
                "Requested configuration value in %s not available for PSP %s. Available PSPs: %s"
                        .formatted(configurationType, psp, availablePsps)
        );
    }
}
