package it.pagopa.ecommerce.commons.utils.bean.redirect.configuration;

/**
 * Enumeration of redirect url mapping criteria
 */
public enum RedirectUrlMappingCriteria {
    /**
     * Method payment type code
     */
    PAYMENT_TYPE_CODE,
    /**
     * PSP unique identifier
     */
    PSP_ID,
    /**
     * Touchpoint identifier
     */
    TOUCHPOINT,
    /**
     * PSP Node channel identifier
     */
    PSP_CHANNEL_ID
}
