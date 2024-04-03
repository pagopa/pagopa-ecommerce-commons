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
     * @param message       error message
     * @param paymentMethod the payment method for which configuration parsing
     *                      failed
     */
    public NpgApiKeyConfigurationException(
            String message,
            NpgClient.PaymentMethod paymentMethod
    ) {
        super(
                "Error parsing NPG PSP api keys configuration for payment method: [%s], cause: %s"
                        .formatted(paymentMethod, message)
        );
    }

    /**
     * Constructor
     *
     * @param message error message
     */
    public NpgApiKeyConfigurationException(
            String message
    ) {
        super(message);
    }
}
