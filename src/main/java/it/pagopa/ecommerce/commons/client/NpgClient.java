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
    private static final AttributeKey<String> NPG_CORRELATION_ID_ATTRIBUTE_NAME = AttributeKey
            .stringKey("npg.correlation_id");

    private static final AttributeKey<List<String>> NPG_ERROR_CODES_ATTRIBUTE_NAME = AttributeKey
            .stringArrayKey("npg.error_codes");

    private static final AttributeKey<Long> NPG_HTTP_ERROR_CODE = AttributeKey
            .longKey("npg.http_error_code");
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
     * Enumeration of NPG gateway operations and associated span names
     */
    private enum GatewayOperation {
        /**
         * Build form operation: operation that initiate payment phase retrieving card
         * data input fields or to start an APM payment
         */
        BUILD_FORM("NpgClient#buildForm"),
        /**
         * Get card data operation: used for cards payments to retrieve masked user
         * inserted card information such as card bin and so on
         */
        GET_CARD_DATA("NpgClient#getCardData"),
        /**
         * Confirm payment operation: used for cards payment
         */
        CONFIRM_PAYMENT("NpgClient#confirmPayment"),
        /**
         * Refund operation
         */
        REFUND_PAYMENT("NpgClient#refundPayment"),
        /**
         * Get payment state operation
         */
        GET_STATE("NpgClient#getState"),

        /*
         * Get order state
         */
        GET_ORDER("NpgClient#getOrder");

        final String spanName;

        GatewayOperation(String spanName) {
            this.spanName = spanName;
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
        GatewayOperation gatewayOperation = GatewayOperation.BUILD_FORM;
        return Mono.using(
                () -> {
                    paymentServicesApi.getApiClient().setApiKey(defaultApiKey);
                    return tracer.spanBuilder(gatewayOperation.spanName)
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
                        .onErrorMap(err -> exceptionToNpgResponseException(err, span, gatewayOperation)),
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
        GatewayOperation gatewayOperation = GatewayOperation.GET_CARD_DATA;
        return Mono.using(
                () -> {
                    paymentServicesApi.getApiClient().setApiKey(defaultApiKey);
                    return tracer.spanBuilder(gatewayOperation.spanName)
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
                        .onErrorMap(err -> exceptionToNpgResponseException(err, span, gatewayOperation)),
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
        GatewayOperation gatewayOperation = GatewayOperation.CONFIRM_PAYMENT;
        return Mono.using(
                () -> {
                    paymentServicesApi.getApiClient().setApiKey(pspApiKey);
                    return tracer.spanBuilder(gatewayOperation.spanName)
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
                        .onErrorMap(err -> exceptionToNpgResponseException(err, span, gatewayOperation)),
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
        GatewayOperation gatewayOperation = GatewayOperation.REFUND_PAYMENT;
        return Mono.using(
                () -> {
                    paymentServicesApi.getApiClient().setApiKey(null);
                    return tracer.spanBuilder(gatewayOperation.spanName)
                            .setParent(Context.current().with(Span.current()))
                            .setAttribute(NPG_CORRELATION_ID_ATTRIBUTE_NAME, correlationId.toString())
                            .startSpan();
                },
                span -> paymentServicesApi.pspApiV1OperationsOperationIdRefundsPost(
                        operationId,
                        correlationId,
                        defaultApiKey,
                        idempotenceKey.toString(),
                        buildRefundRequestDto(grandTotal, description)
                ).doOnError(
                        WebClientResponseException.class,
                        e -> log.info(
                                NPG_LOG_ERROR_MESSAGE,
                                e.getStatusCode()
                        )
                )
                        .onErrorMap(err -> exceptionToNpgResponseException(err, span, gatewayOperation)),
                Span::end
        );
    }

    /**
     * method to request the authorization state using a sessionId passed as input.
     *
     * @param correlationId the unique id to identify the rest api invocation
     * @param sessionId     the session id used for retrieve a card data
     * @param pspApiKey     the specific psp API key
     * @return An object containing the state of the transaction and the info about
     *         operation details.
     */

    public Mono<StateResponseDto> getState(
                                           @NotNull UUID correlationId,
                                           @NotNull String sessionId,
                                           @NonNull String pspApiKey
    ) {
        GatewayOperation gatewayOperation = GatewayOperation.GET_STATE;
        return Mono.using(
                () -> {
                    paymentServicesApi.getApiClient().setApiKey(pspApiKey);
                    return tracer.spanBuilder(gatewayOperation.spanName)
                            .setParent(Context.current().with(Span.current()))
                            .setAttribute(NPG_CORRELATION_ID_ATTRIBUTE_NAME, correlationId.toString())
                            .startSpan();
                },
                span -> paymentServicesApi.pspApiV1BuildStateGet(correlationId, sessionId)
                        .doOnError(
                                WebClientResponseException.class,
                                e -> log.info(
                                        NPG_LOG_ERROR_MESSAGE,
                                        e.getStatusCode()
                                )
                        )
                        .onErrorMap(err -> exceptionToNpgResponseException(err, span, gatewayOperation)),
                Span::end
        );
    }

    /**
     * Method to get order details and all related operations
     *
     * @param correlationId the unique id to identify the rest api invocation
     * @param pspApiKey     the specific psp API key
     * @param orderId       the orderId of the payment
     * @return An object containing the state of the order and all operations
     *         related to.
     */
    public Mono<OrderResponseDto> getOrder(
                                           UUID correlationId,
                                           String pspApiKey,
                                           String orderId
    ) {
        final var gatewayOperation = GatewayOperation.GET_ORDER;
        return Mono.using(
                () -> tracer.spanBuilder(gatewayOperation.spanName)
                        .setParent(Context.current().with(Span.current()))
                        .setAttribute(NPG_CORRELATION_ID_ATTRIBUTE_NAME, correlationId.toString())
                        .startSpan(),
                span -> paymentServicesApi.pspApiV1OrdersOrderIdGet(correlationId, orderId, pspApiKey)
                        .doOnError(
                                WebClientResponseException.class,
                                e -> log.info(
                                        NPG_LOG_ERROR_MESSAGE,
                                        e.getStatusCode()
                                )
                        )
                        .onErrorMap(err -> exceptionToNpgResponseException(err, span, gatewayOperation)),
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
        String orderBuildAmount = Optional.ofNullable(totalAmount).map(Object::toString)
                .orElse(CREATE_HOSTED_ORDER_REQUEST_PAY_AMOUNT);
        log.info(
                "Creating order build request for payment service: [{}] with amount: [{}]",
                paymentMethod.serviceName,
                orderBuildAmount
        );
        return new CreateHostedOrderRequestDto()
                .version(CREATE_HOSTED_ORDER_REQUEST_VERSION)
                .merchantUrl(merchantUrl.toString())
                .order(
                        new OrderDto()
                                .orderId(orderId)
                                .amount(orderBuildAmount)
                                .currency(CREATE_HOSTED_ORDER_REQUEST_CURRENCY_EUR)
                                .customerId(customerId)
                )
                .paymentSession(
                        new PaymentSessionDto()
                                .actionType(ActionTypeDto.PAY)
                                .amount(orderBuildAmount)
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
                                                                 Span span,
                                                                 GatewayOperation gatewayOperation
    ) {
        List<String> errors = List.of();
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

                errors = Optional
                        .ofNullable(responseErrors)
                        .orElse(List.of())
                        .stream()
                        .map(ErrorsInnerDto::getCode).toList();
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
                NPG_ERROR_CODES_ATTRIBUTE_NAME,
                errors
        );
        span.setAttribute(
                NPG_HTTP_ERROR_CODE,
                statusCode.map(HttpStatus::value).orElse(0)
        );
        span.setStatus(StatusCode.ERROR);

        return new NpgResponseException(
                "Error while invoke method for %s".formatted(gatewayOperation.spanName),
                errors,
                statusCode,
                err
        );
    }
}
