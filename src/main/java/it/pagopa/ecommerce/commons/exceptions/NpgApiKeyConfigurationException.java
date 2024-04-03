package it.pagopa.ecommerce.commons.exceptions;

import it.pagopa.ecommerce.commons.client.NpgClient;

/**
 * Exception thrown when NPG per PSP api key configuration cannot be
 * successfully parsed
 */
public class NpgApiKeyConfigurationException extends RuntimeException {

    /**
     * Constructor
     *
     * @param errorCause    error cause description
     * @param paymentMethod the payment method for which configuration parsing
     *                      failed
     */
    public NpgApiKeyConfigurationException(
            String errorCause,
            NpgClient.PaymentMethod paymentMethod
    ) {
        super(
                "Error parsing NPG PSP api keys configuration for payment method: [%s], cause: %s"
                        .formatted(paymentMethod, errorCause)
        );
    }

    /**
     * Constructor
     *
     * @param errorCause error cause description
     */
    public NpgApiKeyConfigurationException(
            String errorCause
    ) {
        super(errorCause);
    }
}
