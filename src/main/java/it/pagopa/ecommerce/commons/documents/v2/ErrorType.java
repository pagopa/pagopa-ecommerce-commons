package it.pagopa.ecommerce.commons.documents.v2;

/**
 * Enumeration of errors that can happen
 */
public enum ErrorType {
    /**
     * KO response received from Node
     */
    KO_RESPONSE_RECEIVED,
    /**
     * Error happen during communication, no response have been received
     */
    COMMUNICATION_ERROR
}
