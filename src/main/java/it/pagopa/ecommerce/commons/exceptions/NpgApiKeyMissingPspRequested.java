package it.pagopa.ecommerce.commons.exceptions;

import java.util.Set;

/**
 * Exception thrown when requesting an API key from NPG configuration for a
 * nonexisting PSP
 */
public class NpgApiKeyMissingPspRequested extends RuntimeException {

    /**
     * Constructor
     *
     * @param psp           requested PSP
     * @param availablePsps currently configured PSPs
     */
    public NpgApiKeyMissingPspRequested(
            String psp,
            Set<String> availablePsps
    ) {
        super(
                "Requested API key for PSP %s. Available PSPs: %s"
                        .formatted(psp, availablePsps)
        );
    }
}
