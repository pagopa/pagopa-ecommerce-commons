package it.pagopa.ecommerce.commons.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import it.pagopa.ecommerce.commons.exceptions.NpgResponseException;
import it.pagopa.ecommerce.commons.generated.npg.v1.api.PaymentServicesApi;
import it.pagopa.ecommerce.commons.generated.npg.v1.dto.*;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * <p>
 * NpgClient instance to communicate with npg.
 * </p>
 */
@Slf4j
public class NpgClient {

    private static final String CREATE_HOSTED_ORDER_REQUEST_VERSION = "2";
    private static final String CREATE_HOSTED_ORDER_REQUEST_PAY_AMOUNT = "1";
    private static final String CREATE_HOSTED_ORDER_REQUEST_CURRENCY_EUR = "EUR";
    private static final String CREATE_HOSTED_ORDER_REQUEST_LANGUAGE_ITA = "ITA";
    private static final String NPG_CORRELATION_ID_ATTRIBUTE_NAME = "npg.correlation_id";

    private static final String NPG_ERROR_CODES_ATTRIBUTE_NAME = "npg.error_codes";
    private static final String EUR_CURRENCY = "EUR";

    /**
     * The npg Api
     */
    private final PaymentServicesApi paymentServicesApi;

    private final Tracer tracer;

    private final ObjectMapper objectMapper;

    private static final String NPG_LOG_ERROR_MESSAGE = "Got bad response from npg-service [HTTP {}]";

    /**
     * <p>
     * Enumeration for payment methods which NPG can do payments with.
     * </p>
     * <p>
     * Maps to the `paymentService` field when initiating a payment.
     * </p>
     */
    public enum PaymentMethod {
        /**
         * Credit cards
         */
        CARDS("CARDS"),
        /**
         * PayPal
         */
        PAYPAL("PAYPAL"),
        /**
         * PayPal with Pay in 3 option
         */
        PAYPAL_PAGAIN3("PAYPAL_PAGAIN3"),
        /**
         * GiroPay
         */
        GIROPAY("GIROPAY"),
        /**
         * Ideal
         */
        IDEAL("IDEAL"),
        /**
         * MyBank
         */
        MYBANK("MYBANK"),
        /**
         * Google Pay
         */
        GOOGLEPAY("GOOGLEPAY"),
        /**
         * Apple Pay
         */
        APPLEPAY("APPLEPAY"),
        /**
         * Bancomat Pay
         */
        BANCOMATPAY("BANCOMATPAY"),
        /**
         * Bancontact
         */
        BANCONTACT("BANCONTACT"),
        /**
         * Multibanco
         */
        MULTIBANCO("MULTIBANCO"),
        /**
         * WeChat
         */
        WECHAT("WECHAT"),
        /**
         * AliPay
         */
        ALIPAY("ALIPAY"),
        /**
         * PIS (3DS2 Payment Initiation Service)
         */
        PIS("PIS");

        /**
         * API value for `serviceName`
         */
        public final String serviceName;

        PaymentMethod(String serviceName) {
            this.serviceName = serviceName;
        }

        /**
         * Retrieves a {@link PaymentMethod} by its service name
         *
         * @param serviceName the service name
         * @return the corresponding payment method
         * @throws IllegalArgumentException if no payment method exists for the given
         *                                  service name
         */
        public static PaymentMethod fromServiceName(String serviceName) {
            for (PaymentMethod paymentMethod : PaymentMethod.values()) {
                if (paymentMethod.serviceName.equals(serviceName)) {
                    return paymentMethod;
                }
            }

            throw new IllegalArgumentException("Invalid payment method service name: '%s'".formatted(serviceName));
        }
    }

    /**
     * Enumeration for possible errors produced by NPG
     */
    public enum GatewayError {
        /**
         * Merchant url may contain syntax errors
         **/
        GW0001("Merchant url may contain syntax errors"),
        /**
         * The configuration of this field is invalid or not found
         **/
        GW0002("The configuration of this field is invalid or not found"),
        /**
         * The language configuration of this field is invalid or not found
         **/
        GW0003("The language configuration of this field is invalid or not found"),
        /**
         * Payment session with given Session-Id is not found
         **/
        GW0004("Payment session with given Session-Id is not found"),
        /**
         * Invalid jwt claims
         **/
        GW0005("Invalid jwt claims"),
        /**
         * The requested value has not been found
         **/
        GW0006("The requested value has not been found"),
        /**
         * Order not found
         **/
        GW0007("Order not found"),
        /**
         * This operation is not allowed
         **/
        GW0008("This operation is not allowed"),
        /**
         * This operation is not allowed in the current state
         **/
        GW0009("This operation is not allowed in the current state"),
        /**
         * The reached payment state is not valid
         **/
        GW0010("The reached payment state is not valid"),
        /**
         * Invalid payment method
         **/
        GW0011("Invalid payment method"),
        /**
         * The provided field is not a valid TEXT type
         **/
        GW0012("The provided field is not a valid TEXT type"),
        /**
         * Configuration of input TEXT fields is not completed
         **/
        GW0013("Configuration of input TEXT fields is not completed"),
        /**
         * The provided configuration is not a valid ACTION
         **/
        GW0014("The provided configuration is not a valid ACTION"),
        /**
         * Payment set up failed. Card information may be wrong
         **/
        GW0015("Payment set up failed. Card information may be wrong"),
        /**
         * Payment set up failed due to internal errors
         **/
        GW0016("Payment set up failed due to internal errors"),
        /**
         * An operation of the same type already exist
         **/
        GW0017("An operation of the same type already exist"),
        /**
         * The provided order id cannot be null
         **/
        GW0018("The provided order id cannot be null"),
        /**
         * Order amount can not be null or less than zero
         **/
        GW0019("Order amount can not be null or less than zero"),
        /**
         * Installment amounts can not be null or less than zero
         **/
        GW0020("Installment amounts can not be null or less than zero"),
        /**
         * Amounts can not be literals
         **/
        GW0021("Amounts can not be literals"),
        /**
         * Language format is not valid
         **/
        GW0022("Language format is not valid"),
        /**
         * Customer id can not be empty
         **/
        GW0023("Customer id can not be empty"),
        /**
         * Order with the same id already exists
         **/
        GW0024("Order with the same id already exists"),
        /**
         * Merchant not enabled for this apm
         **/
        GW0025("Merchant not enabled for this apm"),
        /**
         * The terminal provided doesn't exist
         **/
        GW0026("The terminal provided doesn't exist"),
        /**
         * Internal Rest communication error during payment
         **/
        GW0027("Internal Rest communication error during payment"),
        /**
         * Order recurring information not found
         **/
        GW0028("Order recurring information not found"),
        /**
         * Order recurring information cannot be retrieved
         **/
        GW0029("Order recurring information cannot be retrieved"),
        /**
         * Invalid GDI URL
         **/
        GW0030("Invalid GDI URL"),
        /**
         * Authorization Bearer is invalid or null
         **/
        GW0031("Authorization Bearer is invalid or null"),
        /**
         * Payment validation failed due to internal errors
         **/
        GW0032("Payment validation failed due to internal errors"),
        /**
         * No operation related to the order has been found
         **/
        GW0033("No operation related to the order has been found"),
        /**
         * Payment failed due to internal errors
         **/
        GW0034("Payment failed due to internal errors"),
        /**
         * We encounter an internal error
         **/
        GW0035("We encounter an internal error"),
        /**
         * This api can't be called while paying with an APM
         **/
        GW0036("This api can't be called while paying with an APM"),
        /**
         * Required all the card's values before calling this api
         **/
        GW0037("Required all the card's values before calling this api"),
        /**
         * Service temporarily unavailable
         **/
        GW0038("Service temporarily unavailable"),
        /**
         * Invalid Apm
         **/
        GW0039("Invalid Apm"),
        /**
         * Invalid field
         **/
        GW0040("Invalid field"),
        /**
         * Hosted log not found
         **/
        GW0041("Hosted log not found"),
        /**
         * Transaction not found
         **/
        GW0042("Transaction not found"),
        /**
         * The order doesn't meet the requirements for the payment service
         **/
        GW0043("The order doesn't meet the requirements for the payment service"),
        /**
         * The terminal doesn't belong to the same multi acquiring group as the original
         * terminal caller
         **/
        GW0044("The terminal doesn't belong to the same multi acquiring group as the original terminal caller");

        /**
         * Error description
         */
        public final String description;

        GatewayError(String description) {
            this.description = description;
        }
    }

    /**
     * Instantiate a npg-client to establish communication via the npg api
     *
     * @param paymentServicesApi the api
     * @param tracer             the OpenTelemetry {@link Tracer} used to add
     *                           monitoring info to this client
     * @param objectMapper       object mapper used to decode error response bodies
     */
    public NpgClient(
            @NotNull PaymentServicesApi paymentServicesApi,
            @NotNull Tracer tracer,
            @NotNull ObjectMapper objectMapper
    ) {
        this.paymentServicesApi = paymentServicesApi;
        this.tracer = tracer;
        this.objectMapper = objectMapper;
    }

    /**
     * method to invoke the orders/build api in order to start a guest payment
     * session, retrieve the sessionId and sessionToken and the fields of the form
     * to display in the webview. This method ensures that the request dto for the
     * orders/build api will be built in the right way (it is easy to build it
     * manually with wrong values, e.g. <i>amount</i> or <i>currency</i> as a string
     * can be easily confused).
     *
     * @param correlationId   the unique id to identify the rest api invocation
     * @param merchantUrl     the merchant url of the payment session
     * @param resultUrl       the result url where the user should be redirected at
     *                        the end of the payment session
     * @param notificationUrl the notification url where notify the session
     * @param cancelUrl       the url where the user should be redirected if the
     *                        session is canceled by the user
     * @param orderId         the orderId of the payment session
     * @param customerId      the customerId url of the api
     * @param paymentMethod   the payment method for which the form should be built
     * @param defaultApiKey   default API key
     * @return An object containing sessionId, sessionToken and the fields list to
     *         show on the client-side
     */
    /*
     * @formatter:off
     *
     * Warning java:S107 - Methods should not have too many parameters
     * Suppressed because this method wraps the underlying API which has this many parameters
     *
     * @formatter:on
     */
    @SuppressWarnings("java:S107")
    public Mono<FieldsDto> buildForm(
                                     @NotNull UUID correlationId,
                                     @NotNull URI merchantUrl,
                                     @NotNull URI resultUrl,
                                     @NotNull URI notificationUrl,
                                     @NotNull URI cancelUrl,
                                     @NotNull String orderId,
                                     @NotNull String customerId,
                                     @NonNull PaymentMethod paymentMethod,
                                     @NonNull String defaultApiKey
    ) {
        return executeBuildForm(
                correlationId,
                merchantUrl,
                resultUrl,
                notificationUrl,
                cancelUrl,
                orderId,
                customerId,
                paymentMethod,
                defaultApiKey,
                null,
                null
        );
    }

    /**
     * method to invoke the orders/build api in order to start a payment session for
     * subsequent payment, retrieve the sessionId and state. This method ensures
     * that the request dto for the orders/build api will be built in the right way
     * (it is easy to build it manually with wrong values, e.g. <i>amount</i> or
     * <i>currency</i> as a string can be easily confused).
     *
     * @param correlationId   the unique id to identify the rest api invocation
     * @param merchantUrl     the merchant url of the payment session
     * @param resultUrl       the result url where the user should be redirected at
     *                        the end of the payment session
     * @param notificationUrl the notification url where notify the session
     * @param cancelUrl       the url where the user should be redirected if the
     *                        session is canceled by the user
     * @param orderId         the orderId of the payment session
     * @param customerId      the customerId url of the api
     * @param paymentMethod   the payment method for which the form should be built
     * @param defaultApiKey   default API key
     * @param contractId      the wallet contractId
     * @return An object containing sessionId and state
     */
    /*
     * @formatter:off
     *
     * Warning java:S107 - Methods should not have too many parameters
     * Suppressed because this method wraps the underlying API which has this many parameters
     *
     * @formatter:on
     */
    @SuppressWarnings("java:S107")
    public Mono<FieldsDto> buildForm(
                                     @NotNull UUID correlationId,
                                     @NotNull URI merchantUrl,
                                     @NotNull URI resultUrl,
                                     @NotNull URI notificationUrl,
                                     @NotNull URI cancelUrl,
                                     @NotNull String orderId,
                                     @NotNull String customerId,
                                     @NonNull PaymentMethod paymentMethod,
                                     @NonNull String defaultApiKey,
                                     String contractId
    ) {
        return executeBuildForm(
                correlationId,
                merchantUrl,
                resultUrl,
                notificationUrl,
                cancelUrl,
                orderId,
                customerId,
                paymentMethod,
                defaultApiKey,
                contractId,
                null
        );
    }

    /**
     * method to invoke the orders/build api in order to start a payment session for
     * subsequent payment, retrieve the sessionId and state. This method ensures
     * that the request dto for the orders/build api will be built in the right way
     * (it is easy to build it manually with wrong values, e.g. <i>amount</i> or
     * <i>currency</i> as a string can be easily confused).
     *
     * @param correlationId   the unique id to identify the rest api invocation
     * @param merchantUrl     the merchant url of the payment session
     * @param resultUrl       the result url where the user should be redirected at
     *                        the end of the payment session
     * @param notificationUrl the notification url where notify the session
     * @param cancelUrl       the url where the user should be redirected if the
     *                        session is canceled by the user
     * @param orderId         the orderId of the payment session
     * @param customerId      the customerId url of the api
     * @param paymentMethod   the payment method for which the form should be built
     * @param defaultApiKey   default API key
     * @param contractId      the wallet contractId
     * @param totalAmount     payment total amount in eurocent
     * @return An object containing sessionId and state
     */
    /*
     * @formatter:off
     *
     * Warning java:S107 - Methods should not have too many parameters
     * Suppressed because this method wraps the underlying API which has this many parameters
     *
     * @formatter:on
     */
    @SuppressWarnings("java:S107")
    public Mono<FieldsDto> buildFormForPayment(
                                               @NotNull UUID correlationId,
                                               @NotNull URI merchantUrl,
                                               @NotNull URI resultUrl,
                                               @NotNull URI notificationUrl,
                                               @NotNull URI cancelUrl,
                                               @NotNull String orderId,
                                               @NotNull String customerId,
                                               @NonNull PaymentMethod paymentMethod,
                                               @NonNull String defaultApiKey,
                                               String contractId,
                                               Integer totalAmount
    ) {
        return executeBuildForm(
                correlationId,
                merchantUrl,
                resultUrl,
                notificationUrl,
                cancelUrl,
                orderId,
                customerId,
                paymentMethod,
                defaultApiKey,
                contractId,
                totalAmount
        );
    }

    /*
     * @formatter:off
     *
     * Warning java:S107 - Methods should not have too many parameters
     * Suppressed because this method wraps the underlying API which has this many parameters
     *
     * @formatter:on
     */
    @SuppressWarnings("java:S107")
    private Mono<FieldsDto> executeBuildForm(
                                             @NotNull UUID correlationId,
                                             @NotNull URI merchantUrl,
                                             @NotNull URI resultUrl,
                                             @NotNull URI notificationUrl,
                                             @NotNull URI cancelUrl,
                                             @NotNull String orderId,
                                             @NotNull String customerId,
                                             @NonNull PaymentMethod paymentMethod,
                                             @NonNull String defaultApiKey,
                                             String contractId,
                                             Integer totalAmount
    ) {
        return Mono.using(
                () -> {
                    paymentServicesApi.getApiClient().setApiKey(defaultApiKey);
                    return tracer.spanBuilder("NpgClient#buildForm")
                            .setParent(Context.current().with(Span.current()))
                            .setAttribute(NPG_CORRELATION_ID_ATTRIBUTE_NAME, correlationId.toString())
                            .startSpan();
                },
                span -> paymentServicesApi.pspApiV1OrdersBuildPost(
                        correlationId,
                        buildOrderRequestDto(
                                merchantUrl,
                                resultUrl,
                                notificationUrl,
                                cancelUrl,
                                orderId,
                                customerId,
                                paymentMethod,
                                contractId,
                                totalAmount
                        )
                ).doOnError(
                        WebClientResponseException.class,
                        e -> log.info(
                                NPG_LOG_ERROR_MESSAGE,
                                e.getStatusCode()
                        )
                )
                        .onErrorMap(err -> exceptionToNpgResponseException(err, span)),
                span -> {
                    paymentServicesApi.getApiClient().setApiKey(null);
                    span.end();
                }
        );
    }

    /**
     * method for retrieving the card data value using a sessionId passed as input.
     *
     * @param correlationId the unique id to identify the rest api invocation
     * @param sessionId     the session id used for retrieve a card data
     * @param defaultApiKey default API key
     * @return An object containing sessionId, sessionToken and the fields list to
     *         show on the client-side
     */
    public Mono<CardDataResponseDto> getCardData(
                                                 @NotNull UUID correlationId,
                                                 @NotNull String sessionId,
                                                 @NonNull String defaultApiKey

    ) {

        return Mono.using(
                () -> {
                    paymentServicesApi.getApiClient().setApiKey(defaultApiKey);
                    return tracer.spanBuilder("NpgClient#getCardData")
                            .setParent(Context.current().with(Span.current()))
                            .setAttribute(NPG_CORRELATION_ID_ATTRIBUTE_NAME, correlationId.toString())
                            .startSpan();
                },
                span -> paymentServicesApi.pspApiV1BuildCardDataGet(
                        correlationId,
                        sessionId
                ).doOnError(
                        WebClientResponseException.class,
                        e -> log.info(
                                NPG_LOG_ERROR_MESSAGE,
                                e.getStatusCode()
                        )
                )
                        .onErrorMap(err -> exceptionToNpgResponseException(err, span)),
                span -> {
                    paymentServicesApi.getApiClient().setApiKey(null);
                    span.end();
                }
        );
    }

    /**
     * method for confirming the payment with the selected PSP.
     *
     * @param correlationId the unique id to identify the rest api invocation
     * @param sessionId     the session id used for retrieve a card data
     * @param grandTotal    ground total which is the sum of amount and fees
     * @param pspApiKey     API key related to a PSP
     * @return An object containing sessionId, sessionToken and the fields list to
     *         show on the client-side
     */
    public Mono<StateResponseDto> confirmPayment(
                                                 @NotNull UUID correlationId,
                                                 @NotNull String sessionId,
                                                 @NotNull BigDecimal grandTotal,
                                                 @NonNull String pspApiKey
    ) {

        return Mono.using(
                () -> {
                    paymentServicesApi.getApiClient().setApiKey(pspApiKey);
                    return tracer.spanBuilder("NpgClient#confirmPayment")
                            .setParent(Context.current().with(Span.current()))
                            .setAttribute(NPG_CORRELATION_ID_ATTRIBUTE_NAME, correlationId.toString())
                            .startSpan();
                },
                span -> paymentServicesApi.pspApiV1BuildConfirmPaymentPost(
                        correlationId,
                        new ConfirmPaymentRequestDto()
                                .amount(String.valueOf(grandTotal.toString())).sessionId(sessionId)
                ).doOnError(
                        WebClientResponseException.class,
                        e -> log.info(
                                NPG_LOG_ERROR_MESSAGE,
                                e.getStatusCode()
                        )
                )
                        .onErrorMap(err -> exceptionToNpgResponseException(err, span)),
                span -> {
                    paymentServicesApi.getApiClient().setApiKey(null);
                    span.end();
                }
        );
    }

    /**
     * method to request the payment refund using a sessionId passed as input.
     *
     * @param correlationId  the unique id to identify the rest api invocation
     * @param operationId    the unique id used to identify a payment operation
     * @param idempotenceKey the idempotenceKey used to identify a refund reqeust
     *                       for the same transaction
     * @param grandTotal     the grand total to be refunded
     * @param defaultApiKey  default API key
     * @param description    the description of the refund request. Not mandatory.
     * @return An object containing the state of the transaction and the info about
     *         operation details.
     */
    public Mono<RefundResponseDto> refundPayment(
                                                 @NotNull UUID correlationId,
                                                 @NotNull String operationId,
                                                 @NotNull UUID idempotenceKey,
                                                 @NotNull BigDecimal grandTotal,
                                                 @NonNull String defaultApiKey,
                                                 String description
    ) {
        return Mono.using(
                () -> {
                    paymentServicesApi.getApiClient().setApiKey(null);
                    return tracer.spanBuilder("NpgClient#refundPayment")
                            .setParent(Context.current().with(Span.current()))
                            .setAttribute(NPG_CORRELATION_ID_ATTRIBUTE_NAME, correlationId.toString())
                            .startSpan();
                },
                span -> paymentServicesApi.pspApiV1OperationsOperationIdRefundsPost(
                        operationId,
                        correlationId,
                        idempotenceKey.toString(),
                        defaultApiKey,
                        buildRefundRequestDto(grandTotal, description)
                ).doOnError(
                        WebClientResponseException.class,
                        e -> log.info(
                                NPG_LOG_ERROR_MESSAGE,
                                e.getStatusCode()
                        )
                )
                        .onErrorMap(err -> exceptionToNpgResponseException(err, span)),
                Span::end
        );
    }

    /*
     * @formatter:off
     *
     * Warning java:S107 - Methods should not have too many parameters
     * Suppressed because this method wraps the underlying API which has this many parameters
     *
     * @formatter:on
     */
    @SuppressWarnings("java:S107")
    private CreateHostedOrderRequestDto buildOrderRequestDto(
                                                             URI merchantUrl,
                                                             URI resultUrl,
                                                             URI notificationUrl,
                                                             URI cancelUrl,
                                                             String orderId,
                                                             String customerId,
                                                             PaymentMethod paymentMethod,
                                                             String contractId,
                                                             Integer totalAmount
    ) {
        return new CreateHostedOrderRequestDto()
                .version(CREATE_HOSTED_ORDER_REQUEST_VERSION)
                .merchantUrl(merchantUrl.toString())
                .order(
                        new OrderDto()
                                .orderId(orderId)
                                .amount(
                                        Optional.ofNullable(totalAmount).map(Object::toString)
                                                .orElse(CREATE_HOSTED_ORDER_REQUEST_PAY_AMOUNT)
                                )
                                .currency(CREATE_HOSTED_ORDER_REQUEST_CURRENCY_EUR)
                                .customerId(customerId)
                )
                .paymentSession(
                        new PaymentSessionDto()
                                .actionType(ActionTypeDto.PAY)
                                .amount(
                                        Optional.ofNullable(totalAmount).map(Object::toString)
                                                .orElse(CREATE_HOSTED_ORDER_REQUEST_PAY_AMOUNT)
                                )
                                .language(CREATE_HOSTED_ORDER_REQUEST_LANGUAGE_ITA)
                                .paymentService(paymentMethod.serviceName)
                                .resultUrl(resultUrl.toString())
                                .cancelUrl(cancelUrl.toString())
                                .notificationUrl(notificationUrl.toString())
                                .recurrence(
                                        contractId != null ? new RecurringSettingsDto()
                                                .action(RecurringActionDto.SUBSEQUENT_PAYMENT)
                                                .contractType(RecurringContractTypeDto.CIT).contractId(contractId)
                                                : null
                                )
                );
    }

    private RefundRequestDto buildRefundRequestDto(
                                                   BigDecimal grandTotal,
                                                   String description
    ) {
        return new RefundRequestDto().amount(grandTotal.toString()).currency(EUR_CURRENCY)
                .description(description);
    }

    private NpgResponseException exceptionToNpgResponseException(
                                                                 Throwable err,
                                                                 Span span
    ) {
        List<GatewayError> errors = List.of();
        Optional<HttpStatus> statusCode = Optional.empty();

        if (err instanceof WebClientResponseException e) {
            try {
                List<ErrorsInnerDto> responseErrors = switch (e.getStatusCode()) {
                    case INTERNAL_SERVER_ERROR -> objectMapper.readValue(
                            e.getResponseBodyAsByteArray(),
                            ServerErrorDto.class
                    ).getErrors();
                    case BAD_REQUEST -> objectMapper.readValue(
                            e.getResponseBodyAsByteArray(),
                            ClientErrorDto.class
                    ).getErrors();
                    default -> List.of();
                };

                errors = responseErrors.stream()
                        .map(error -> GatewayError.valueOf(error.getCode())).toList();
                statusCode = Optional.of(e.getStatusCode());
            } catch (IOException ex) {
                String errorMessage = "Invalid error response from NPG with status code %s";
                log.error(errorMessage.formatted(e.getStatusCode()));

                return new NpgResponseException(
                        errorMessage.formatted(e.getStatusCode()),
                        Optional.of(e.getStatusCode()),
                        ex
                );
            }
        }

        span.setAttribute(
                AttributeKey.stringArrayKey(NPG_ERROR_CODES_ATTRIBUTE_NAME),
                errors.stream().map(GatewayError::name).toList()
        );
        span.setStatus(StatusCode.ERROR);

        return new NpgResponseException(
                "Error while invoke method for build order",
                errors,
                statusCode,
                err
        );
    }
}
