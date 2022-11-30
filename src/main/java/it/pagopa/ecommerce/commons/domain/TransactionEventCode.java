package it.pagopa.ecommerce.commons.domain;

/**
 * <p>
 *     Enumeration describing all possible events.
 * </p>
 */
public enum TransactionEventCode {
    /**
     * @see it.pagopa.ecommerce.commons.generated.events.v1.TransactionActivatedEvent TransactionActivatedEvent
     */
    TRANSACTION_ACTIVATED_EVENT("TRANSACTION_ACTIVATED_EVENT"),
    /**
     * @see it.pagopa.ecommerce.commons.generated.events.v1.TransactionActivationRequestedEvent TransactionActivationRequestedEvent
     */
    TRANSACTION_ACTIVATION_REQUESTED_EVENT(
            "TRANSACTION_ACTIVATION_REQUESTED_EVENT"
    ),
    /**
     * @see it.pagopa.ecommerce.commons.generated.events.v1.TransactionAuthorizationRequestedEvent TransactionAuthorizationRequestedEvent
     */
    TRANSACTION_AUTHORIZATION_REQUESTED_EVENT(
            "TRANSACTION_AUTHORIZATION_REQUESTED_EVENT"
    ),
    /**
     * @see it.pagopa.ecommerce.commons.generated.events.v1.TransactionAuthorizationStatusUpdatedEvent TransactionAuthorizationStatusUpdatedEvent
     */
    TRANSACTION_AUTHORIZATION_STATUS_UPDATED_EVENT(
            "TRANSACTION_AUTHORIZATION_STATUS_UPDATED_EVENT"
    ),
    /**
     * @see it.pagopa.ecommerce.commons.generated.events.v1.TransactionClosureSentEvent TransactionClosureSentEvent
     */
    TRANSACTION_CLOSURE_SENT_EVENT(
            "TRANSACTION_CLOSURE_SENT_EVENT"
    ),
    /**
     * @see it.pagopa.ecommerce.commons.generated.events.v1.TransactionClosureErrorEvent TransactionClosureErrorEvent
     */
    TRANSACTION_CLOSURE_ERROR_EVENT("TRANSACTION_CLOSURE_ERROR_EVENT"),
    /**
     * @see it.pagopa.ecommerce.commons.generated.events.v1.TransactionUserReceiptAddedEvent TransactionUserReceiptAddedEvent
     */
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
