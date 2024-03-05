package it.pagopa.ecommerce.commons.exceptions;

import java.util.Set;

/**
 * Exception thrown when requesting an API key from NPG configuration for a
 * nonexisting PSP
 */
public class RedirectMissingPspRequestedException extends RuntimeException {

    /**
     * Constructor
     *
     * @param psp               requested PSP
     * @param availablePsps     currently configured PSPs
     * @param configurationType configuration type
     */
    public RedirectMissingPspRequestedException(
            String psp,
            Set<String> availablePsps,
            RedirectConfigurationType configurationType
    ) {
        super(
                "Requested configuration value in %s not available for PSP %s. Available PSPs: %s"
                        .formatted(configurationType, psp, availablePsps)
        );
    }
}
