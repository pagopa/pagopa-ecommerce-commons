package it.pagopa.ecommerce.commons.domain;

/**
 * <p>
 *     Enumeration describing all possible events.
 * </p>
 */
public enum TransactionEventCode {
    TRANSACTION_ACTIVATED_EVENT("TRANSACTION_ACTIVATED_EVENT"),
    TRANSACTION_ACTIVATION_REQUESTED_EVENT(
            "TRANSACTION_ACTIVATION_REQUESTED_EVENT"
    ),
    TRANSACTION_AUTHORIZATION_REQUESTED_EVENT(
            "TRANSACTION_AUTHORIZATION_REQUESTED_EVENT"
    ),
    TRANSACTION_AUTHORIZATION_STATUS_UPDATED_EVENT(
            "TRANSACTION_AUTHORIZATION_STATUS_UPDATED_EVENT"
    ),
    TRANSACTION_CLOSURE_SENT_EVENT(
            "TRANSACTION_CLOSURE_SENT_EVENT"
    ),

    TRANSACTION_CLOSURE_ERROR_EVENT("TRANSACTION_CLOSURE_ERROR_EVENT"),
    TRANSACTION_USER_RECEIPT_ADDED_EVENT(
            "TRANSACTION_USER_RECEIPT_ADDED_EVENT"
    );

    private final String code;

    TransactionEventCode(final String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return code;
    }
}
