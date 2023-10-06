package it.pagopa.ecommerce.commons.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;
import it.pagopa.ecommerce.commons.exceptions.NpgResponseException;
import it.pagopa.ecommerce.commons.generated.npg.v1.ApiClient;
import it.pagopa.ecommerce.commons.generated.npg.v1.api.PaymentServicesApi;
import it.pagopa.ecommerce.commons.generated.npg.v1.dto.*;
import it.pagopa.ecommerce.commons.queues.StrictJsonSerializerProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class NpgClientTests {
    private static final String MOCKED_API_KEY = "mocked-api-key";
    private static final String ORDER_REQUEST_VERSION = "2";
    private static final String MERCHANT_URL = "localhost/merchant";
    private static final String ORDER_REQUEST_ORDER_ID = "orderId";
    private static final String ORDER_REQUEST_PAY = "1";
    private static final String ORDER_REQUEST_CURRENCY_EUR = "EUR";
    private static final String ORDER_REQUEST_CUSTOMER_ID = "customerId";
    private static final String ORDER_REQUEST_PAYMENT_SERVICE_CARDS = NpgClient.PaymentMethod.CARDS.serviceName;
    private static final String ORDER_REQUEST_LANGUAGE_ITA = "ITA";
    private static final String CANCEL_URL = "localhost/cancel";
    private static final String NOTIFICATION_URL = "localhost/notification";
    private static final String RESULT_URL = "localhost/result";
    private static final String SESSION_ID = "sessionId";
    private static final String SECURITY_TOKEN = "securityToken";
    private static final String TEST_1 = "test1";
    private static final String SRC_1 = "src1";
    private static final String PROPERTY_1 = "property1";
    private static final String TYPE_1 = "type1";
    private static final String BIN = "123456";
    private static final String CIRCUIT = "VISA";
    private static final String OPERATION_ID = "OPERATION_ID";
    private static final UUID IDEMPOTENCE_KEY = UUID.randomUUID();
    private static final String CURRENCY = "EUR";
    private static final String AMOUNT = "1000";
    @Mock
    private ApiClient apiClient;
    @Mock
    @Qualifier("npgWebClient")
    private PaymentServicesApi paymentServicesApi;

    @Mock
    private Tracer tracer;

    private NpgClient npgClient;

    private final ObjectMapper objectMapper = new StrictJsonSerializerProvider().getObjectMapper();

    @BeforeEach
    public void init() {

        SpanBuilder spanBuilder = Mockito.mock(SpanBuilder.class);
        Mockito.when(spanBuilder.setParent(any())).thenReturn(spanBuilder);
        Mockito.when(spanBuilder.setAttribute(anyString(), anyString())).thenReturn(spanBuilder);
        Mockito.when(spanBuilder.startSpan()).thenReturn(Span.getInvalid());
        Mockito.when(tracer.spanBuilder(anyString())).thenReturn(spanBuilder);

        npgClient = new NpgClient(paymentServicesApi, tracer, objectMapper);
    }

    @Test
    void shouldRetrieveFieldsDtoUsingExplicitParameters() {
        FieldsDto fieldsDto = buildTestFieldsDto();

        UUID correlationUUID = UUID.randomUUID();
        CreateHostedOrderRequestDto requestDto = buildCreateHostedOrderRequestDto();

        Mockito.when(
                paymentServicesApi.pspApiV1OrdersBuildPost(
                        correlationUUID,
                        MOCKED_API_KEY,
                        requestDto
                )
        ).thenReturn(Mono.just(fieldsDto));

        StepVerifier
                .create(
                        npgClient.buildForm(
                                correlationUUID,
                                URI.create(MERCHANT_URL),
                                URI.create(RESULT_URL),
                                URI.create(NOTIFICATION_URL),
                                URI.create(CANCEL_URL),
                                ORDER_REQUEST_ORDER_ID,
                                ORDER_REQUEST_CUSTOMER_ID,
                                NpgClient.PaymentMethod.CARDS,
                                MOCKED_API_KEY
                        )
                )
                .expectNext(fieldsDto)
                .verifyComplete();
    }

    @Test
    void shouldThrowExceptionWhenBuildFormThrows() throws JsonProcessingException {
        UUID correlationUUID = UUID.randomUUID();
        CreateHostedOrderRequestDto requestDto = buildCreateHostedOrderRequestDto();

        Mockito.when(
                paymentServicesApi.pspApiV1OrdersBuildPost(
                        correlationUUID,
                        MOCKED_API_KEY,
                        requestDto
                )
        )
                .thenReturn(
                        Mono.error(
                                new WebClientResponseException(
                                        "Test error when calling apiOrdersBuildPost",
                                        HttpStatus.BAD_REQUEST.value(),
                                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                        null,
                                        objectMapper.writeValueAsBytes(
                                                npgClientErrorResponse(NpgClient.GatewayError.GW0001)
                                        ),
                                        null
                                )
                        )
                );

        StepVerifier
                .create(
                        npgClient.buildForm(
                                correlationUUID,
                                URI.create(MERCHANT_URL),
                                URI.create(RESULT_URL),
                                URI.create(NOTIFICATION_URL),
                                URI.create(CANCEL_URL),
                                ORDER_REQUEST_ORDER_ID,
                                ORDER_REQUEST_CUSTOMER_ID,
                                NpgClient.PaymentMethod.CARDS,
                                MOCKED_API_KEY
                        )
                )
                .expectError(NpgResponseException.class)
                .verify();
    }

    @Test
    void shouldPropagateErrorCodesWhenBuildFormThrows() throws JsonProcessingException {
        UUID correlationUUID = UUID.randomUUID();
        CreateHostedOrderRequestDto requestDto = buildCreateHostedOrderRequestDto();

        Mockito.when(
                paymentServicesApi.pspApiV1OrdersBuildPost(
                        correlationUUID,
                        MOCKED_API_KEY,
                        requestDto
                )
        )
                .thenReturn(
                        Mono.error(
                                new WebClientResponseException(
                                        "Test error when calling apiOrdersBuildPost",
                                        HttpStatus.BAD_REQUEST.value(),
                                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                        null,
                                        objectMapper.writeValueAsBytes(
                                                npgClientErrorResponse(NpgClient.GatewayError.GW0001)
                                        ),
                                        null
                                )
                        )
                );

        StepVerifier
                .create(
                        npgClient.buildForm(
                                correlationUUID,
                                URI.create(MERCHANT_URL),
                                URI.create(RESULT_URL),
                                URI.create(NOTIFICATION_URL),
                                URI.create(CANCEL_URL),
                                ORDER_REQUEST_ORDER_ID,
                                ORDER_REQUEST_CUSTOMER_ID,
                                NpgClient.PaymentMethod.CARDS,
                                MOCKED_API_KEY
                        )
                )
                .expectError(NpgResponseException.class)
                .verify();
    }

    @Test
    void shouldRetrieveCardDataWithSuccess() {
        UUID correlationUUID = UUID.randomUUID();
        CardDataResponseDto expectedResponse = new CardDataResponseDto().bin(BIN).circuit(CIRCUIT).expiringDate("0426")
                .lastFourDigits("1234");
        Mockito.when(
                paymentServicesApi.pspApiV1BuildCardDataGet(
                        correlationUUID,
                        MOCKED_API_KEY,
                        SESSION_ID
                )
        ).thenReturn(Mono.just(expectedResponse));

        StepVerifier
                .create(
                        npgClient.getCardData(
                                correlationUUID,
                                SESSION_ID,
                                MOCKED_API_KEY
                        )
                )
                .expectNext(expectedResponse)
                .verifyComplete();
    }

    @Test
    void shouldThrowExceptionWhileRetrieveCardData() throws JsonProcessingException {
        UUID correlationUUID = UUID.randomUUID();

        Mockito.when(
                paymentServicesApi.pspApiV1BuildCardDataGet(
                        correlationUUID,
                        MOCKED_API_KEY,
                        SESSION_ID
                )
        )
                .thenReturn(
                        Mono.error(
                                new WebClientResponseException(
                                        "Error while invoke method for get card data",
                                        HttpStatus.NOT_FOUND.value(),
                                        HttpStatus.NOT_FOUND.getReasonPhrase(),
                                        null,
                                        objectMapper.writeValueAsBytes(
                                                npgClientErrorResponse(NpgClient.GatewayError.GW0001)
                                        ),
                                        null
                                )
                        )
                );

        StepVerifier
                .create(
                        npgClient.getCardData(
                                correlationUUID,
                                SESSION_ID,
                                MOCKED_API_KEY
                        )
                )
                .expectError(NpgResponseException.class)
                .verify();
    }

    @Test
    void shouldPropagateErrorCodesWhileRetrieveCardData() throws JsonProcessingException {
        UUID correlationUUID = UUID.randomUUID();

        Mockito.when(
                paymentServicesApi.pspApiV1BuildCardDataGet(
                        correlationUUID,
                        MOCKED_API_KEY,
                        SESSION_ID
                )
        )
                .thenReturn(
                        Mono.error(
                                new WebClientResponseException(
                                        "Error while invoke method for get card data",
                                        HttpStatus.BAD_REQUEST.value(),
                                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                        null,
                                        objectMapper.writeValueAsBytes(
                                                npgClientErrorResponse(NpgClient.GatewayError.GW0001)
                                        ),
                                        null
                                )
                        )
                );

        StepVerifier
                .create(
                        npgClient.getCardData(
                                correlationUUID,
                                SESSION_ID,
                                MOCKED_API_KEY
                        )
                )
                .expectErrorMatches(
                        e -> e instanceof NpgResponseException npgResponseException
                                && npgResponseException.getErrors().equals(List.of(NpgClient.GatewayError.GW0001))
                )
                .verify();
    }

    @Test
    void shouldConfirmPaymentGivenValidNpgSession() {
        StateResponseDto stateResponseDto = buildTestRetrieveStateResponseDto();

        UUID correlationUUID = UUID.randomUUID();
        ConfirmPaymentRequestDto confirmPaymentRequestDto = buildTestConfirmPaymentRequestDto();

        Mockito.when(
                paymentServicesApi.pspApiV1BuildConfirmPaymentPost(
                        correlationUUID,
                        MOCKED_API_KEY,
                        confirmPaymentRequestDto
                )
        ).thenReturn(Mono.just(stateResponseDto));

        StepVerifier
                .create(
                        npgClient.confirmPayment(
                                correlationUUID,
                                SESSION_ID,
                                new BigDecimal(ORDER_REQUEST_PAY),
                                MOCKED_API_KEY
                        )
                )
                .expectNext(stateResponseDto)
                .verifyComplete();
    }

    @Test
    void shouldRefundPaymentGivenValidNpgSession() {
        RefundResponseDto refundResponseDto = buildTestRefundResponseDto();

        UUID correlationUUID = UUID.randomUUID();
        RefundRequestDto refundRequestDto = buildRefundRequestDto();

        Mockito.when(
                paymentServicesApi.pspApiV1OperationsOperationIdRefundsPost(
                        OPERATION_ID,
                        correlationUUID,
                        MOCKED_API_KEY,
                        IDEMPOTENCE_KEY.toString(),
                        refundRequestDto
                )
        ).thenReturn(Mono.just(refundResponseDto));

        StepVerifier
                .create(
                        npgClient.refundPayment(
                                correlationUUID,
                                OPERATION_ID,
                                IDEMPOTENCE_KEY,
                                BigDecimal.valueOf(Integer.parseInt(AMOUNT)),
                                MOCKED_API_KEY,
                                null
                        )
                )
                .expectNext(refundResponseDto)
                .verifyComplete();
    }

    @Test
    void shouldPropagateErrorCodesWhileConfirmPayment() throws JsonProcessingException {
        UUID correlationUUID = UUID.randomUUID();
        ConfirmPaymentRequestDto confirmPaymentRequestDto = buildTestConfirmPaymentRequestDto();

        Mockito.when(
                paymentServicesApi.pspApiV1BuildConfirmPaymentPost(
                        correlationUUID,
                        MOCKED_API_KEY,
                        confirmPaymentRequestDto
                )
        )
                .thenReturn(
                        Mono.error(
                                new WebClientResponseException(
                                        "Error while invoke method for confirm payment",
                                        HttpStatus.BAD_REQUEST.value(),
                                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                        null,
                                        objectMapper.writeValueAsBytes(
                                                npgClientErrorResponse(NpgClient.GatewayError.GW0001)
                                        ),
                                        null
                                )
                        )
                );

        StepVerifier
                .create(
                        npgClient.confirmPayment(
                                correlationUUID,
                                SESSION_ID,
                                new BigDecimal(ORDER_REQUEST_PAY),
                                MOCKED_API_KEY
                        )
                )
                .expectErrorMatches(
                        e -> e instanceof NpgResponseException npgResponseException
                                && npgResponseException.getErrors().equals(List.of(NpgClient.GatewayError.GW0001))
                )
                .verify();
    }

    @Test
    void shouldPropagateErrorCodesWhileRefundPayment() throws JsonProcessingException {
        UUID correlationUUID = UUID.randomUUID();
        RefundRequestDto refundRequestDto = buildRefundRequestDto();

        Mockito.when(
                paymentServicesApi.pspApiV1OperationsOperationIdRefundsPost(
                        OPERATION_ID,
                        correlationUUID,
                        MOCKED_API_KEY,
                        IDEMPOTENCE_KEY.toString(),
                        refundRequestDto
                )
        )
                .thenReturn(
                        Mono.error(
                                new WebClientResponseException(
                                        "Error while invoke method for confirm payment",
                                        HttpStatus.BAD_REQUEST.value(),
                                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                        null,
                                        objectMapper.writeValueAsBytes(
                                                npgClientErrorResponse(NpgClient.GatewayError.GW0001)
                                        ),
                                        null
                                )
                        )
                );

        StepVerifier
                .create(
                        npgClient.refundPayment(
                                correlationUUID,
                                OPERATION_ID,
                                IDEMPOTENCE_KEY,
                                BigDecimal.valueOf(Integer.parseInt(AMOUNT)),
                                MOCKED_API_KEY,
                                null
                        )
                )
                .expectErrorMatches(
                        e -> e instanceof NpgResponseException npgResponseException
                                && npgResponseException.getErrors().equals(List.of(NpgClient.GatewayError.GW0001))
                )
                .verify();
    }

    private static ClientErrorDto npgClientErrorResponse(NpgClient.GatewayError gatewayError) {
        return new ClientErrorDto()
                .errors(
                        List.of(
                                new ErrorsInnerDto()
                                        .code(gatewayError.name())
                                        .description(gatewayError.description)
                        )
                );
    }

    private SessionIdRequestDto buildSessionIdRequestDto() {
        return new SessionIdRequestDto()
                .sessionId(SESSION_ID);
    }

    private RefundRequestDto buildRefundRequestDto() {
        return new RefundRequestDto()
                .amount(AMOUNT)
                .currency(CURRENCY);
    }

    private CreateHostedOrderRequestDto buildCreateHostedOrderRequestDto() {
        return new CreateHostedOrderRequestDto()
                .version(ORDER_REQUEST_VERSION)
                .merchantUrl(MERCHANT_URL)
                .order(
                        new OrderDto()
                                .orderId(ORDER_REQUEST_ORDER_ID)
                                .amount(ORDER_REQUEST_PAY)
                                .currency(ORDER_REQUEST_CURRENCY_EUR)
                                .customerId(ORDER_REQUEST_CUSTOMER_ID)
                )
                .paymentSession(
                        new PaymentSessionDto()
                                .paymentService(ORDER_REQUEST_PAYMENT_SERVICE_CARDS)
                                .amount(ORDER_REQUEST_PAY)
                                .actionType(ActionTypeDto.PAY)
                                .language(ORDER_REQUEST_LANGUAGE_ITA)
                                .cancelUrl(CANCEL_URL)
                                .notificationUrl(NOTIFICATION_URL)
                                .resultUrl(RESULT_URL)
                );
    }

    private FieldsDto buildTestFieldsDto() {
        return new FieldsDto()
                .sessionId(SESSION_ID)
                .securityToken(SECURITY_TOKEN)
                .fields(
                        List.of(
                                new FieldDto().id(TEST_1).src(SRC_1).propertyClass(PROPERTY_1).type(TYPE_1)
                        )
                );
    }

    private StateResponseDto buildTestRetrieveStateResponseDto() {
        return new StateResponseDto()
                .url("https://iframe-gdi")
                .state(StateDto.REDIRECTED_TO_EXTERNAL_DOMAIN);
    }

    private RefundResponseDto buildTestRefundResponseDto() {
        return new RefundResponseDto().operationId("operation-id").operationTime("2022-09-01T01:20:00.001Z");
    }

    private ConfirmPaymentRequestDto buildTestConfirmPaymentRequestDto() {
        return new ConfirmPaymentRequestDto()
                .sessionId(SESSION_ID)
                .amount(ORDER_REQUEST_PAY);
    }

}
