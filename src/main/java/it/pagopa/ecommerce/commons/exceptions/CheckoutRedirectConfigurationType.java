package it.pagopa.ecommerce.commons.exceptions;

/**
 * Enumeration containing possible types of Checkout Redirect configuration.
 * Each enum variant maps to a different configuration property. These are kept
 * separate to separate secrets from common configurations.
 */
public enum CheckoutRedirectConfigurationType {
    /**
     * Configuration for PSP API keys
     */
    API_KEYS,
    /**
     * Configuration for URLs for the Checkout Redirect PSP API
     */
    BACKEND_URLS,
    /**
     * Configuration for PSPs logos
     */
    LOGOS
}
