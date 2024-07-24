package it.pagopa.ecommerce.commons.utils;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.trace.Tracer;
import it.pagopa.ecommerce.commons.documents.v2.Transaction;

import javax.validation.constraints.NotNull;
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
         * Transaction status update triggered by PGS (VPOS)
         */
        PGS_VPOS,

        /**
         * Transaction status update triggered by PGS (XPAY)
         */
        PGS_XPAY,

        /**
         * Transaction status update triggered by PSP (through redirect payment flow
         * integration)
         */
        REDIRECT,

        /**
         * Used when cannot derive transaction status update trigger
         */
        UNKNOWN
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
                        statusUpdateInfo.type().toString()
                )
                .put(
                        UPDATE_TRANSACTION_STATUS_OUTCOME_ATTRIBUTE_KEY,
                        statusUpdateInfo.outcome().toString()
                )
                .put(
                        UPDATE_TRANSACTION_STATUS_TRIGGER_ATTRIBUTE_KEY,
                        statusUpdateInfo.trigger().toString()
                )
                .put(
                        UPDATE_TRANSACTION_STATUS_PSP_ID_ATTRIBUTE_KEY,
                        statusUpdateInfo.pspId().orElse(FIELD_NOT_AVAILABLE)
                )
                .put(
                        UPDATE_TRANSACTION_STATUS_GATEWAY_OUTCOME_ATTRIBUTE_KEY,
                        statusUpdateInfo.gatewayOutcomeResult().map(GatewayOutcomeResult::gatewayOperationOutcome)
                                .orElse(FIELD_NOT_AVAILABLE)
                )
                .put(
                        UPDATE_TRANSACTION_STATUS_GATEWAY_ERROR_CODE_ATTRIBUTE_KEY,
                        statusUpdateInfo.gatewayOutcomeResult().flatMap(GatewayOutcomeResult::errorCode)
                                .orElse(FIELD_NOT_AVAILABLE)
                )
                .put(
                        UPDATE_TRANSACTION_STATUS_PAYMENT_METHOD_TYPE_CODE_ATTRIBUTE_KEY,
                        statusUpdateInfo.paymentMethodTypeCode()
                )
                .put(
                        UPDATE_TRANSACTION_STATUS_CLIENT_ID_ATTRIBUTE_KEY,
                        statusUpdateInfo.clientId().toString()
                )
                .put(
                        UPDATE_TRANSACTION_STATUS_WALLET_PAYMENT_ATTRIBUTE_KEY,
                        statusUpdateInfo.isWalletPayment()
                );
        if (statusUpdateInfo.isWalletPayment() != null) {
            spanAttributes.put(
                    UPDATE_TRANSACTION_STATUS_WALLET_PAYMENT_ATTRIBUTE_KEY,
                    statusUpdateInfo.isWalletPayment()
            );
        }

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
     * @param isWalletPayment       boolean value indicating if the transaction have
     *                              been performed with an onboarded method or not
     *                              (wallet)
     * @param gatewayOutcomeResult  gateway outcome result
     */
    public record SendPaymentResultNodoStatusUpdate(
            UpdateTransactionStatusOutcome outcome,
            Optional<String> pspId,
            String paymentMethodTypeCode,
            Transaction.ClientId clientId,
            Boolean isWalletPayment,
            Optional<GatewayOutcomeResult> gatewayOutcomeResult
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
         * @param isWalletPayment       boolean value indicating if the transaction have
         *                              been performed with an onboarded method or not
         *                              (wallet)
         * @param gatewayOutcomeResult  gateway operation result
         */
        public SendPaymentResultNodoStatusUpdate {
            Objects.requireNonNull(outcome);
            Objects.requireNonNull(pspId);
            Objects.requireNonNull(paymentMethodTypeCode);
            Objects.requireNonNull(clientId);
            Objects.requireNonNull(isWalletPayment);
            Objects.requireNonNull(gatewayOutcomeResult);
        }

        @Override
        public UpdateTransactionStatusType type() {
            return UpdateTransactionStatusType.SEND_PAYMENT_RESULT_OUTCOME;
        }

        @Override
        public UpdateTransactionTrigger trigger() {
            return UpdateTransactionTrigger.NODO;
        }

    }

    /**
     * Transaction status update record for Nodo sendPaymentResult operation
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
            Optional<String> pspId,
            Optional<String> paymentTypeCode,
            Transaction.ClientId clientId,
            Optional<Boolean> walletPayment,
            Optional<GatewayOutcomeResult> gatewayOutcomeResult
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
        public UpdateTransactionStatusType type() {
            return UpdateTransactionStatusType.CLOSE_PAYMENT_OUTCOME;
        }

        @Override
        public UpdateTransactionTrigger trigger() {
            return UpdateTransactionTrigger.NODO;
        }

        @Override
        public String paymentMethodTypeCode() {
            return this.paymentTypeCode.orElse(FIELD_NOT_AVAILABLE);
        }

        @Override
        public Boolean isWalletPayment() {
            return this.walletPayment.orElse(null);
        }
    }

    /**
     * Contextual data for a transaction authorization status update
     *
     * @param trigger               the gateway trigger that initiate the request
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
            @NotNull UpdateTransactionTrigger trigger,
            @NotNull Optional<String> pspId,
            @NotNull Optional<GatewayOutcomeResult> gatewayOutcomeResult,
            @NotNull String paymentMethodTypeCode,
            @NotNull Transaction.ClientId clientId,
            @NotNull Boolean isWalletPayment
    ) {
        /**
         * Perform checks against required fields
         *
         * @param trigger               the gateway trigger that initiate the request
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
            Objects.requireNonNull(trigger);
            Objects.requireNonNull(pspId);
            Objects.requireNonNull(gatewayOutcomeResult);
            Objects.requireNonNull(paymentMethodTypeCode);
            Objects.requireNonNull(clientId);
            Objects.requireNonNull(isWalletPayment);
            if (!Set.of(
                    UpdateTransactionTrigger.NPG,
                    UpdateTransactionTrigger.PGS_XPAY,
                    UpdateTransactionTrigger.PGS_VPOS,
                    UpdateTransactionTrigger.REDIRECT,
                    UpdateTransactionTrigger.UNKNOWN
            ).contains(trigger)) {
                throw new IllegalArgumentException(
                        "Invalid trigger for PaymentGatewayStatusUpdate: %s".formatted(trigger)
                );
            }
        }
    }

    /**
     * Transaction status update record for payment transaction gateway update
     * trigger
     *
     * @param outcome - the transaction update status outcome
     * @param context - the transaction update status context
     */
    public record PaymentGatewayStatusUpdate(
            @NotNull UpdateTransactionStatusOutcome outcome,
            @NotNull PaymentGatewayStatusUpdateContext context
    )
            implements
            StatusUpdateInfo {

        /**
         * Primary constructor
         *
         * @param outcome authorization status outcome
         * @param context contextual information about the authorization status update
         */
        public PaymentGatewayStatusUpdate {
            Objects.requireNonNull(outcome);
            Objects.requireNonNull(context);
        }

        @Override
        public UpdateTransactionStatusType type() {
            return UpdateTransactionStatusType.AUTHORIZATION_OUTCOME;
        }

        @Override
        public UpdateTransactionTrigger trigger() {
            return context.trigger;
        }

        @Override
        public Optional<String> pspId() {
            return context.pspId;
        }

        @Override
        public Optional<GatewayOutcomeResult> gatewayOutcomeResult() {
            return context.gatewayOutcomeResult;
        }

        @Override
        public String paymentMethodTypeCode() {
            return context.paymentMethodTypeCode;
        }

        @Override
        public Boolean isWalletPayment() {
            return context.isWalletPayment;
        }

        @Override
        public Transaction.ClientId clientId() {
            return context.clientId;
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
        UpdateTransactionStatusType type();

        /**
         * @return the update transaction trigger
         * @see UpdateTransactionTrigger
         */
        UpdateTransactionTrigger trigger();

        /**
         * @return the update transaction outcome
         * @see UpdateTransactionStatusOutcome
         */
        UpdateTransactionStatusOutcome outcome();

        /**
         * The id of the psp chosen by the user
         *
         * @return the id of the PSP
         */
        Optional<String> pspId();

        /**
         * The gateway outcome information
         *
         * @return the gateway outcome information
         */
        Optional<GatewayOutcomeResult> gatewayOutcomeResult();

        /**
         * The client id the transaction comes from
         *
         * @return the client identifier
         */
        Transaction.ClientId clientId();

        /**
         * The payment method type code of the authorization request
         *
         * @return the payment method type code
         */
        String paymentMethodTypeCode();

        /**
         * Boolean value indicating if the operation have been performed with a wallet
         * onboarded method or not
         *
         * @return true iff the operation is performed with an onboarded method
         */
        Boolean isWalletPayment();
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
}
