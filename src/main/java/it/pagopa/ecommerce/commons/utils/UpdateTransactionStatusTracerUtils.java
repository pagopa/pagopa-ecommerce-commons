package it.pagopa.ecommerce.commons.utils;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.trace.Tracer;
import it.pagopa.ecommerce.commons.documents.v2.Transaction;
import it.pagopa.ecommerce.commons.documents.v2.TransactionAuthorizationRequestData;

import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * This utility class traces transaction update status performed by external
 * entities. Tracing is performed by meaning of OpenTelemetry span creation.
 * {@link UpdateTransactionStatusType} enumeration contains transaction status
 * typologies enumeration {@link UpdateTransactionTrigger} enumeration, instead,
 * contains external actors that trigger the transaction status update
 */
public class UpdateTransactionStatusTracerUtils {

    private final OpenTelemetryUtils openTelemetryUtils;

    /**
     * Span attribute used to discriminate transaction update status operation type
     *
     * @see UpdateTransactionStatusType
     */
    static final AttributeKey<String> UPDATE_TRANSACTION_STATUS_TYPE_ATTRIBUTE_KEY = AttributeKey
            .stringKey("updateTransactionStatus.type");
    /**
     * Span attribute used to trace status update outcome
     *
     * @see UpdateTransactionStatusOutcome
     */
    static final AttributeKey<String> UPDATE_TRANSACTION_STATUS_OUTCOME_ATTRIBUTE_KEY = AttributeKey
            .stringKey("updateTransactionStatus.outcome");
    /**
     * Span attribute used to trace status update trigger
     *
     * @see UpdateTransactionTrigger
     */
    static final AttributeKey<String> UPDATE_TRANSACTION_STATUS_TRIGGER_ATTRIBUTE_KEY = AttributeKey
            .stringKey("updateTransactionStatus.trigger");

    /**
     * Span attribute used to trace transaction psp id (useful to discriminate
     * redirection payment flows
     */
    static final AttributeKey<String> UPDATE_TRANSACTION_STATUS_PSP_ID_ATTRIBUTE_KEY = AttributeKey
            .stringKey("updateTransactionStatus.pspId");

    /**
     * Span attribute used to trace transaction payment method type code
     */
    static final AttributeKey<String> UPDATE_TRANSACTION_STATUS_PAYMENT_METHOD_TYPE_CODE_ATTRIBUTE_KEY = AttributeKey
            .stringKey("updateTransactionStatus.paymentMethodTypeCode");

    /**
     * Span attribute used to trace the id of the client that initiated the
     * transaction
     */
    static final AttributeKey<String> UPDATE_TRANSACTION_STATUS_CLIENT_ID_ATTRIBUTE_KEY = AttributeKey
            .stringKey("updateTransactionStatus.clientId");

    /**
     * Span attribute used to trace whether a wallet was used for authorizing the
     * transaction
     */
    static final AttributeKey<Boolean> UPDATE_TRANSACTION_STATUS_WALLET_PAYMENT_ATTRIBUTE_KEY = AttributeKey
            .booleanKey("updateTransactionStatus.walletPayment");

    /**
     * Span attribute used to trace gateway received authorization outcome
     */
    static final AttributeKey<String> UPDATE_TRANSACTION_STATUS_GATEWAY_OUTCOME_ATTRIBUTE_KEY = AttributeKey
            .stringKey("updateTransactionStatus.gateway.outcome");

    /**
     * Span attribute used to trace gateway received authorization error code
     */
    static final AttributeKey<String> UPDATE_TRANSACTION_STATUS_GATEWAY_ERROR_CODE_ATTRIBUTE_KEY = AttributeKey
            .stringKey("updateTransactionStatus.gateway.errorCode");

    /**
     * Enumeration of all operation that update transaction status performed by
     * external entities
     */
    public enum UpdateTransactionStatusType {

        /**
         * Transaction status update triggered by requested authorization
         */
        AUTHORIZATION_REQUESTED,
        /**
         * Transaction status update operation triggered by payment gateway by receiving
         * authorization outcome
         */
        AUTHORIZATION_OUTCOME,
        /**
         * Transaction status update operation triggered by Nodo with sendPaymentResult
         * operation
         */
        SEND_PAYMENT_RESULT_OUTCOME,

        /**
         * Transaction status update operation triggered automatically on Node close
         * payment response outcome received
         */
        CLOSE_PAYMENT_OUTCOME
    }

    /**
     * Enumeration of all actors that can trigger a transaction status update
     */
    public enum UpdateTransactionTrigger {
        /**
         * Transaction status update triggered by Nodo
         */
        NODO,
        /**
         * Transaction status update triggered by NPG (authorization outcome)
         */
        NPG,

        /**
         * Transaction status update triggered by PSP (through redirect payment flow
         * integration)
         */
        REDIRECT,

        /**
         * Used when cannot derive transaction status update trigger
         */
        UNKNOWN;

        /**
         * Return the associated {@link UpdateTransactionTrigger} to the input
         * {@link TransactionAuthorizationRequestData.PaymentGateway} enum value
         *
         * @param paymentGateway the payment gateway to convert to
         * @return the associated {@link UpdateTransactionTrigger}
         */
        public static UpdateTransactionTrigger from(TransactionAuthorizationRequestData.PaymentGateway paymentGateway) {
            return switch (paymentGateway) {
                case VPOS, XPAY -> throw new RuntimeException("Pgs gateways aren't available anymore");
                case NPG -> NPG;
                case REDIRECT -> REDIRECT;
            };
        }
    }

    /**
     * Enumeration of update transaction status possible outcomes
     */
    public enum UpdateTransactionStatusOutcome {
        /**
         * The transaction is status update has been processed successfully
         */
        OK,
        /**
         * Error processing transaction status update: transaction is in wrong state
         */
        WRONG_TRANSACTION_STATUS,

        /**
         * Error processing transaction status update: cannot found transaction for
         * input transaction id
         */
        TRANSACTION_NOT_FOUND,

        /**
         * Error processing transaction status update: the input request is invalid
         */
        INVALID_REQUEST,

        /**
         * Error processing transaction status update: an unexpected error has occurred
         * processing transaction update state
         */
        PROCESSING_ERROR
    }

    static final String UPDATE_TRANSACTION_STATUS_SPAN_NAME = "Transaction status updated";

    static final String FIELD_NOT_AVAILABLE = "N/A";

    /**
     * Utility constructor that create an {@link UpdateTransactionStatusTracerUtils}
     * instance using {@link OpenTelemetryUtils} that will create spans
     *
     * @param openTelemetryUtils the open telemetry {@link Tracer} instance to be
     *                           used for span creations
     */
    public UpdateTransactionStatusTracerUtils(OpenTelemetryUtils openTelemetryUtils) {
        this.openTelemetryUtils = openTelemetryUtils;
    }

    /**
     * Trace status update operation for the input tracing information
     *
     * @param statusUpdateInfo transaction status update information
     */
    public void traceStatusUpdateOperation(StatusUpdateInfo statusUpdateInfo) {
        AttributesBuilder spanAttributes = Attributes
                .builder()
                .put(
                        UPDATE_TRANSACTION_STATUS_TYPE_ATTRIBUTE_KEY,
                        statusUpdateInfo.getType().toString()
                )
                .put(
                        UPDATE_TRANSACTION_STATUS_OUTCOME_ATTRIBUTE_KEY,
                        statusUpdateInfo.getOutcome().toString()
                )
                .put(
                        UPDATE_TRANSACTION_STATUS_TRIGGER_ATTRIBUTE_KEY,
                        statusUpdateInfo.getTrigger().toString()
                )
                .put(
                        UPDATE_TRANSACTION_STATUS_PSP_ID_ATTRIBUTE_KEY,
                        statusUpdateInfo.getPspId().orElse(FIELD_NOT_AVAILABLE)
                )
                .put(
                        UPDATE_TRANSACTION_STATUS_GATEWAY_OUTCOME_ATTRIBUTE_KEY,
                        statusUpdateInfo.getGatewayOutcomeResult().map(GatewayOutcomeResult::gatewayOperationOutcome)
                                .orElse(FIELD_NOT_AVAILABLE)
                )
                .put(
                        UPDATE_TRANSACTION_STATUS_GATEWAY_ERROR_CODE_ATTRIBUTE_KEY,
                        statusUpdateInfo.getGatewayOutcomeResult().flatMap(GatewayOutcomeResult::errorCode)
                                .orElse(FIELD_NOT_AVAILABLE)
                )
                .put(
                        UPDATE_TRANSACTION_STATUS_PAYMENT_METHOD_TYPE_CODE_ATTRIBUTE_KEY,
                        statusUpdateInfo.getPaymentMethodTypeCode().orElse(FIELD_NOT_AVAILABLE)
                )
                .put(
                        UPDATE_TRANSACTION_STATUS_CLIENT_ID_ATTRIBUTE_KEY,
                        statusUpdateInfo.getClientId().map(Enum::toString).orElse(FIELD_NOT_AVAILABLE)
                );
        statusUpdateInfo.isWalletPayment().ifPresent(
                isWalletPayment -> spanAttributes.put(
                        UPDATE_TRANSACTION_STATUS_WALLET_PAYMENT_ATTRIBUTE_KEY,
                        isWalletPayment
                )
        );

        openTelemetryUtils.addSpanWithAttributes(UPDATE_TRANSACTION_STATUS_SPAN_NAME, spanAttributes.build());
    }

    /**
     * Transaction status update record for Nodo sendPaymentResult operation
     *
     * @param outcome               the transaction update outcome
     * @param pspId                 psp identifier for the current transaction
     * @param paymentMethodTypeCode payment type code used in the current
     *                              transaction
     * @param clientId              client identifier that have initiated the
     *                              transaction
     * @param walletPayment         boolean value indicating if the transaction have
     *                              been performed with an onboarded method or not
     *                              (wallet)
     * @param gatewayOutcomeResult  gateway outcome result
     */
    public record SendPaymentResultNodoStatusUpdate(
            UpdateTransactionStatusOutcome outcome,
            String pspId,
            String paymentMethodTypeCode,
            Transaction.ClientId clientId,
            Boolean walletPayment,
            GatewayOutcomeResult gatewayOutcomeResult
    )
            implements
            StatusUpdateInfo {
        /**
         * Perform check against required fields
         *
         * @param outcome               the transaction update outcome
         * @param pspId                 psp identifier for the current transaction
         * @param paymentMethodTypeCode payment type code used in the current
         *                              transaction
         * @param clientId              client identifier that have initiated the
         *                              transaction
         * @param walletPayment         boolean value indicating if the transaction have
         *                              been performed with an onboarded method or not
         *                              (wallet)
         * @param gatewayOutcomeResult  gateway operation result
         */
        public SendPaymentResultNodoStatusUpdate {
            Objects.requireNonNull(outcome);
            Objects.requireNonNull(pspId);
            Objects.requireNonNull(paymentMethodTypeCode);
            Objects.requireNonNull(clientId);
            Objects.requireNonNull(walletPayment);
            Objects.requireNonNull(gatewayOutcomeResult);
        }

        @Override
        public UpdateTransactionStatusType getType() {
            return UpdateTransactionStatusType.SEND_PAYMENT_RESULT_OUTCOME;
        }

        @Override
        public UpdateTransactionTrigger getTrigger() {
            return UpdateTransactionTrigger.NODO;
        }

        @Override
        public UpdateTransactionStatusOutcome getOutcome() {
            return outcome;
        }

        @Override
        public Optional<String> getPspId() {
            return Optional.of(pspId);
        }

        @Override
        public Optional<GatewayOutcomeResult> getGatewayOutcomeResult() {
            return Optional.of(gatewayOutcomeResult);
        }

        @Override
        public Optional<Transaction.ClientId> getClientId() {
            return Optional.of(clientId);
        }

        @Override
        public Optional<String> getPaymentMethodTypeCode() {
            return Optional.of(paymentMethodTypeCode);
        }

        @Override
        public Optional<Boolean> isWalletPayment() {
            return Optional.of(walletPayment);
        }

    }

    /**
     * Transaction status update record for Nodo close payment operation
     *
     * @param outcome              the transaction update outcome
     * @param pspId                psp identifier for the current transaction
     * @param paymentTypeCode      payment type code used in the current transaction
     *                             (absent for user canceled transaction)
     * @param clientId             client identifier that have initiated the
     *                             transaction
     * @param walletPayment        boolean value indicating if the transaction have
     *                             been performed with an onboarded method (wallet)
     *                             or not (absent for user canceled transaction)
     * @param gatewayOutcomeResult gateway outcome result
     */
    public record ClosePaymentNodoStatusUpdate(
            UpdateTransactionStatusOutcome outcome,
            String pspId,
            String paymentTypeCode,
            Transaction.ClientId clientId,
            Boolean walletPayment,
            GatewayOutcomeResult gatewayOutcomeResult
    )
            implements
            StatusUpdateInfo {
        /**
         * Perform check against required fields
         *
         * @param outcome              the transaction update outcome
         * @param pspId                psp identifier for the current transaction
         * @param paymentTypeCode      payment type code used in the current transaction
         * @param clientId             client identifier that have initiated the
         *                             transaction
         * @param walletPayment        boolean value indicating if the transaction have
         *                             been performed with an onboarded method or not
         *                             (wallet)
         * @param gatewayOutcomeResult gateway operation result
         */
        public ClosePaymentNodoStatusUpdate {
            Objects.requireNonNull(outcome);
            Objects.requireNonNull(pspId);
            Objects.requireNonNull(paymentTypeCode);
            Objects.requireNonNull(clientId);
            Objects.requireNonNull(walletPayment);
            Objects.requireNonNull(gatewayOutcomeResult);
        }

        @Override
        public UpdateTransactionStatusType getType() {
            return UpdateTransactionStatusType.CLOSE_PAYMENT_OUTCOME;
        }

        @Override
        public UpdateTransactionTrigger getTrigger() {
            return UpdateTransactionTrigger.NODO;
        }

        @Override
        public UpdateTransactionStatusOutcome getOutcome() {
            return outcome;
        }

        @Override
        public Optional<String> getPspId() {
            return Optional.of(pspId);
        }

        @Override
        public Optional<GatewayOutcomeResult> getGatewayOutcomeResult() {
            return Optional.of(gatewayOutcomeResult);
        }

        @Override
        public Optional<Transaction.ClientId> getClientId() {
            return Optional.of(clientId);
        }

        @Override
        public Optional<String> getPaymentMethodTypeCode() {
            return Optional.of(this.paymentTypeCode);
        }

        @Override
        public Optional<Boolean> isWalletPayment() {
            return Optional.of(this.walletPayment);
        }
    }

    /**
     * Transaction status update record for Nodo close payment operation
     *
     * @param outcome              the transaction update outcome (absent for user
     *                             canceled transaction)
     * @param clientId             client identifier that have initiated the
     *                             transaction
     * @param gatewayOutcomeResult gateway outcome result
     */
    public record UserCancelClosePaymentNodoStatusUpdate(
            UpdateTransactionStatusOutcome outcome,
            Transaction.ClientId clientId,
            GatewayOutcomeResult gatewayOutcomeResult
    )
            implements
            StatusUpdateInfo {
        /**
         * Perform check against required fields
         *
         * @param outcome              the transaction update outcome
         * @param clientId             client identifier that have initiated the
         *                             transaction
         * @param gatewayOutcomeResult gateway operation result
         */
        public UserCancelClosePaymentNodoStatusUpdate {
            Objects.requireNonNull(outcome);
            Objects.requireNonNull(clientId);
            Objects.requireNonNull(gatewayOutcomeResult);
        }

        @Override
        public UpdateTransactionStatusType getType() {
            return UpdateTransactionStatusType.CLOSE_PAYMENT_OUTCOME;
        }

        @Override
        public UpdateTransactionTrigger getTrigger() {
            return UpdateTransactionTrigger.NODO;
        }

        @Override
        public UpdateTransactionStatusOutcome getOutcome() {
            return outcome;
        }

        @Override
        public Optional<String> getPspId() {
            return Optional.empty();
        }

        @Override
        public Optional<GatewayOutcomeResult> getGatewayOutcomeResult() {
            return Optional.of(gatewayOutcomeResult);
        }

        @Override
        public Optional<Transaction.ClientId> getClientId() {
            return Optional.of(clientId);
        }

        @Override
        public Optional<String> getPaymentMethodTypeCode() {
            return Optional.empty();
        }

        @Override
        public Optional<Boolean> isWalletPayment() {
            return Optional.empty();
        }
    }

    /**
     * Contextual data for a transaction authorization status update
     *
     * @param pspId                 the psp id chosen for the current transaction
     * @param gatewayOutcomeResult  the gateway authorization outcome result
     * @param paymentMethodTypeCode payment type code used in the current
     *                              transaction
     * @param clientId              client identifier that have initiated the
     *                              transaction
     * @param isWalletPayment       boolean value indicating if the transaction have
     *                              been performed with an onboarded method or not
     *                              (wallet)
     */
    public record PaymentGatewayStatusUpdateContext(
            @NotNull String pspId,
            @NotNull GatewayOutcomeResult gatewayOutcomeResult,
            @NotNull String paymentMethodTypeCode,
            @NotNull Transaction.ClientId clientId,
            @NotNull Boolean isWalletPayment
    ) {
        /**
         * Perform checks against required fields
         *
         * @param pspId                 the psp id chosen for the current transaction
         * @param gatewayOutcomeResult  the gateway authorization outcome result
         * @param paymentMethodTypeCode payment type code used in the current
         *                              transaction
         * @param clientId              client identifier that have initiated the
         *                              transaction
         * @param isWalletPayment       boolean value indicating if the transaction have
         *                              been performed with an onboarded method or not
         *                              (wallet)
         */
        public PaymentGatewayStatusUpdateContext {
            Objects.requireNonNull(pspId);
            Objects.requireNonNull(gatewayOutcomeResult);
            Objects.requireNonNull(paymentMethodTypeCode);
            Objects.requireNonNull(clientId);
            Objects.requireNonNull(isWalletPayment);
        }
    }

    /**
     * Transaction status update record for payment transaction gateway update
     * trigger
     *
     * @param trigger - the gateway trigger that initiate the request
     * @param outcome - the transaction update status outcome
     * @param context - the transaction update status context
     */
    public record PaymentGatewayStatusUpdate(
            @NotNull UpdateTransactionTrigger trigger,
            @NotNull UpdateTransactionStatusOutcome outcome,
            @NotNull PaymentGatewayStatusUpdateContext context
    )
            implements
            StatusUpdateInfo {

        /**
         * Primary constructor
         *
         * @param trigger the gateway trigger that initiate the request
         * @param outcome authorization status outcome
         * @param context contextual information about the authorization status update
         */
        public PaymentGatewayStatusUpdate {
            Objects.requireNonNull(trigger);
            if (!Set.of(
                    UpdateTransactionTrigger.NPG,
                    UpdateTransactionTrigger.REDIRECT,
                    UpdateTransactionTrigger.UNKNOWN
            ).contains(trigger)) {
                throw new IllegalArgumentException(
                        "Invalid trigger for PaymentGatewayStatusUpdate: %s".formatted(trigger)
                );
            }

            Objects.requireNonNull(outcome);
            if (outcome == UpdateTransactionStatusOutcome.INVALID_REQUEST) {
                throw new IllegalArgumentException(
                        "Invalid outcome for `PaymentGatewayStatusUpdate`: `INVALID_REQUEST`"
                );
            }

            Objects.requireNonNull(context);
        }

        @Override
        public UpdateTransactionStatusType getType() {
            return UpdateTransactionStatusType.AUTHORIZATION_OUTCOME;
        }

        @Override
        public UpdateTransactionTrigger getTrigger() {
            return trigger;
        }

        @Override
        public UpdateTransactionStatusOutcome getOutcome() {
            return outcome;
        }

        @Override
        public Optional<String> getPspId() {
            return Optional.of(context.pspId);
        }

        @Override
        public Optional<GatewayOutcomeResult> getGatewayOutcomeResult() {
            return Optional.of(context.gatewayOutcomeResult);
        }

        @Override
        public Optional<String> getPaymentMethodTypeCode() {
            return Optional.of(context.paymentMethodTypeCode);
        }

        @Override
        public Optional<Boolean> isWalletPayment() {
            return Optional.of(context.isWalletPayment);
        }

        @Override
        public Optional<Transaction.ClientId> getClientId() {
            return Optional.of(context.clientId);
        }
    }

    /**
     * Authorization requested status update info
     *
     * @param trigger              authorization request trigger
     * @param outcome              the transaction update outcome
     * @param pspId                psp identifier for the current transaction
     *                             (absent for user canceled transaction)
     * @param clientId             client identifier that have initiated the
     *                             transaction
     * @param walletPayment        boolean value indicating if the transaction have
     *                             been performed with an onboarded method (wallet)
     *                             or not (absent for user canceled transaction)
     * @param gatewayOutcomeResult authorization result
     */
    public record AuthorizationRequestedStatusUpdate(
            @NotNull UpdateTransactionTrigger trigger,
            @NotNull UpdateTransactionStatusOutcome outcome,
            @NotNull String pspId,
            @NotNull String paymentMethodTypeCode,
            @NotNull Transaction.ClientId clientId,
            @NotNull Boolean walletPayment,

            @NotNull GatewayOutcomeResult gatewayOutcomeResult
    )
            implements
            StatusUpdateInfo {
        /**
         * Default constructor implementation that checks for required attributes to be
         * present
         *
         * @param trigger               authorization request trigger
         * @param outcome               the transaction update outcome
         * @param pspId                 psp identifier for the current transaction
         *                              (absent for user canceled transaction)
         * @param clientId              client identifier that have initiated the
         *                              transaction
         * @param paymentMethodTypeCode payment type code for the current transaction
         * @param walletPayment         boolean value indicating if the transaction have
         *                              been performed with an onboarded method (wallet)
         *                              or not (absent for user canceled transaction)
         * @param gatewayOutcomeResult  authorization result
         */
        public AuthorizationRequestedStatusUpdate {
            Objects.requireNonNull(trigger);
            Objects.requireNonNull(outcome);
            Objects.requireNonNull(pspId);
            Objects.requireNonNull(paymentMethodTypeCode);
            Objects.requireNonNull(clientId);
            Objects.requireNonNull(walletPayment);
            Objects.requireNonNull(gatewayOutcomeResult);
            if (!Set.of(
                    UpdateTransactionTrigger.NPG,
                    UpdateTransactionTrigger.REDIRECT
            ).contains(trigger)) {
                throw new IllegalArgumentException(
                        "Invalid trigger for AuthorizationRequestedStatusUpdate: %s".formatted(trigger)
                );
            }
        }

        @Override
        public UpdateTransactionStatusType getType() {
            return UpdateTransactionStatusType.AUTHORIZATION_REQUESTED;
        }

        @Override
        public UpdateTransactionTrigger getTrigger() {
            return this.trigger;
        }

        @Override
        public UpdateTransactionStatusOutcome getOutcome() {
            return this.outcome;
        }

        @Override
        public Optional<String> getPspId() {
            return Optional.of(this.pspId);
        }

        @Override
        public Optional<GatewayOutcomeResult> getGatewayOutcomeResult() {
            return Optional.of(gatewayOutcomeResult);
        }

        @Override
        public Optional<Transaction.ClientId> getClientId() {
            return Optional.of(this.clientId);
        }

        @Override
        public Optional<String> getPaymentMethodTypeCode() {
            return Optional.of(this.paymentMethodTypeCode);
        }

        @Override
        public Optional<Boolean> isWalletPayment() {
            return Optional.of(this.walletPayment);
        }
    }

    /**
     * Common interface for all status update information
     */
    public interface StatusUpdateInfo {
        /**
         * @return the update transaction status type
         * @see UpdateTransactionStatusType
         */
        UpdateTransactionStatusType getType();

        /**
         * @return the update transaction trigger
         * @see UpdateTransactionTrigger
         */
        UpdateTransactionTrigger getTrigger();

        /**
         * @return the update transaction outcome
         * @see UpdateTransactionStatusOutcome
         */
        UpdateTransactionStatusOutcome getOutcome();

        /**
         * The id of the psp chosen by the user
         *
         * @return the id of the PSP
         */
        Optional<String> getPspId();

        /**
         * The gateway outcome information
         *
         * @return the gateway outcome information
         */
        Optional<GatewayOutcomeResult> getGatewayOutcomeResult();

        /**
         * The client id the transaction comes from
         *
         * @return the client identifier
         */
        Optional<Transaction.ClientId> getClientId();

        /**
         * The payment method type code of the authorization request
         *
         * @return the payment method type code
         */
        Optional<String> getPaymentMethodTypeCode();

        /**
         * Boolean value indicating if the operation have been performed with a wallet
         * onboarded method or not
         *
         * @return true iff the operation is performed with an onboarded method
         */
        Optional<Boolean> isWalletPayment();
    }

    /**
     * The gateway authorization outcome result
     *
     * @param gatewayOperationOutcome the received gateway operation outcome
     * @param errorCode               the optional authorization error code
     */
    public record GatewayOutcomeResult(
            String gatewayOperationOutcome,
            Optional<String> errorCode
    ) {
    }

    /**
     * Status update representing an error during the status update
     *
     * @param type    the status update of this invalid request
     * @param trigger the component that triggered this invalid update
     * @param outcome the outcome of the status update
     */
    public record ErrorStatusTransactionUpdate(
            @NotNull UpdateTransactionStatusType type,
            @NotNull UpdateTransactionTrigger trigger,
            @NotNull UpdateTransactionStatusOutcome outcome
    )
            implements
            StatusUpdateInfo {

        /**
         * Primary constructor
         *
         * @param type    the status update of this invalid request
         * @param trigger the component that triggered this invalid update
         * @param outcome the outcome of the status update
         */
        public ErrorStatusTransactionUpdate {
            Objects.requireNonNull(type);
            Objects.requireNonNull(trigger);
            if (!Set.of(
                    UpdateTransactionStatusOutcome.WRONG_TRANSACTION_STATUS,
                    UpdateTransactionStatusOutcome.TRANSACTION_NOT_FOUND,
                    UpdateTransactionStatusOutcome.PROCESSING_ERROR,
                    UpdateTransactionStatusOutcome.INVALID_REQUEST
            ).contains(outcome)) {
                throw new IllegalArgumentException(
                        "Invalid outcome for UpdateTransactionStatusOutcome: %s".formatted(outcome)
                );
            }
        }

        @Override
        public UpdateTransactionStatusType getType() {
            return type;
        }

        @Override
        public UpdateTransactionTrigger getTrigger() {
            return trigger;
        }

        @Override
        public UpdateTransactionStatusOutcome getOutcome() {
            return outcome;
        }

        @Override
        public Optional<String> getPspId() {
            return Optional.empty();
        }

        @Override
        public Optional<GatewayOutcomeResult> getGatewayOutcomeResult() {
            return Optional.empty();
        }

        @Override
        public Optional<Transaction.ClientId> getClientId() {
            return Optional.empty();
        }

        @Override
        public Optional<String> getPaymentMethodTypeCode() {
            return Optional.empty();
        }

        @Override
        public Optional<Boolean> isWalletPayment() {
            return Optional.empty();
        }
    }
}
