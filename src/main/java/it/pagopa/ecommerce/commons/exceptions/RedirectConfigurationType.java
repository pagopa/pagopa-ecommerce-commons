package it.pagopa.ecommerce.commons.exceptions;

/**
 * Enumeration containing possible types of Redirect configuration. Each enum
 * variant maps to a different configuration property. These are kept separate
 * to separate secrets from common configurations.
 */
public enum RedirectConfigurationType {
    /**
     * Configuration for PSP API keys
     */
    API_KEYS,
    /**
     * Configuration for URLs for the Redirect PSP API
     */
    BACKEND_URLS,
    /**
     * Configuration for PSPs logos
     */
    LOGOS
}
