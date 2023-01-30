package it.pagopa.ecommerce.commons.domain;

/**
 * Event code identifying event type
 */
public enum TransactionEventCode {

    /**
     * Activation event
     */
    TRANSACTION_ACTIVATED_EVENT("TRANSACTION_ACTIVATED_EVENT"),
    /**
     * Activation request event
     */
    TRANSACTION_ACTIVATION_REQUESTED_EVENT("TRANSACTION_ACTIVATION_REQUESTED_EVENT"),
    /**
     * Payment authorization request event
     */
    TRANSACTION_AUTHORIZATION_REQUESTED_EVENT("TRANSACTION_AUTHORIZATION_REQUESTED_EVENT"),
    /**
     * Authorization status update event
     */
    TRANSACTION_AUTHORIZATION_STATUS_UPDATED_EVENT("TRANSACTION_AUTHORIZATION_STATUS_UPDATED_EVENT"),
    /**
     * Transaction closure event
     */
    TRANSACTION_CLOSURE_SENT_EVENT("TRANSACTION_CLOSURE_SENT_EVENT"),

    /**
     * Error event when transaction closure fails
     */
    TRANSACTION_CLOSURE_ERROR_EVENT("TRANSACTION_CLOSURE_ERROR_EVENT"),
    /**
     * Event when transaction closure is retried
     */
    TRANSACTION_CLOSURE_RETRIED_EVENT("TRANSACTION_CLOSURE_RETRIED_EVENT"),
    /**
     * User receipt notification event
     */
    TRANSACTION_USER_RECEIPT_ADDED_EVENT("TRANSACTION_USER_RECEIPT_ADDED_EVENT"),

    /**
     * Transaction expiration event
     */
    TRANSACTION_EXPIRED_EVENT("TRANSACTION_EXPIRED_EVENT"),
    /**
     * Transaction refund event
     */
    TRANSACTION_REFUNDED_EVENT("TRANSACTION_REFUNDED_EVENT"),
    /**
     * Retry event for transaction refunds
     */
    TRANSACTION_REFUND_RETRY_EVENT("TRANSACTION_REFUND_RETRY_EVENT"),
    /**
     * Event when transaction refund is retried
     */
    TRANSACTION_REFUND_RETRIED_EVENT("TRANSACTION_REFUND_RETRIED_EVENT");

    private final String code;

    TransactionEventCode(final String code) {
        this.code = code;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return code;
    }
}
