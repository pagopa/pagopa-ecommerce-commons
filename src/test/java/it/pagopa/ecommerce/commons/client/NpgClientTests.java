package it.pagopa.ecommerce.commons.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class NpgClientTests {

    private static final Map<String, String> LANG_MAP = Map.of(
            "it",
            "ITA",
            "fr",
            "FRA",
            "de",
            "DEU",
            "sl",
            "SLV",
            "en",
            "ENG"
    );

    private static final String MOCKED_API_KEY = "mocked-api-key";
    private static final String ORDER_REQUEST_VERSION = "2";
    private static final String MERCHANT_URL = "localhost/merchant";
    private static final String ORDER_REQUEST_ORDER_ID = "orderId";
    private static final String ORDER_REQUEST_CONTRACT_ID = "contractId";
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

    private static final String REFUND_DESCRIPTION = "REFUND_DESCRIPTION";
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
        Mockito.when(spanBuilder.setAttribute(any(AttributeKey.class), anyString())).thenReturn(spanBuilder);
        Mockito.when(spanBuilder.startSpan()).thenReturn(Span.getInvalid());

        Mockito.when(tracer.spanBuilder(anyString())).thenReturn(spanBuilder);

        npgClient = new NpgClient(paymentServicesApi, tracer, objectMapper);
    }

    @Test
    void shouldRetrieveFieldsDtoUsingExplicitParameters() {
        FieldsDto fieldsDto = buildTestFieldsDto();

        UUID correlationUUID = UUID.randomUUID();
        CreateHostedOrderRequestDto requestDto = buildCreateHostedOrderRequestDto(null);

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

    @ParameterizedTest
    @ValueSource(
            strings = {
                    "it",
                    "en",
                    "de",
                    "sl",
                    "fr"
            }
    )
    void shouldRetrieveFieldsDtoUsingExplicitParametersWithLangExplicitSet(String input) {

        String ISO_639_3_lang = ORDER_REQUEST_LANGUAGE_ITA;
        if (input != null) {
            ISO_639_3_lang = LANG_MAP.getOrDefault(input, ORDER_REQUEST_LANGUAGE_ITA);
        }

        FieldsDto fieldsDto = buildTestFieldsDto();

        UUID correlationUUID = UUID.randomUUID();
        CreateHostedOrderRequestDto requestDto = buildCreateHostedOrderRequestDto(null, null, ISO_639_3_lang);

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
                                MOCKED_API_KEY,
                                null,
                                input
                        )
                )
                .expectNext(fieldsDto)
                .verifyComplete();
    }

    @Test
    void shouldThrowExceptionWhenBuildFormThrows() throws JsonProcessingException {
        UUID correlationUUID = UUID.randomUUID();
        CreateHostedOrderRequestDto requestDto = buildCreateHostedOrderRequestDto(null);

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
                                        "Test error when calling pspApiV1OrdersBuildPost",
                                        HttpStatus.BAD_REQUEST.value(),
                                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                        null,
                                        objectMapper.writeValueAsBytes(
                                                npgClientErrorResponse("GW0001")
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
    void shouldThrowExceptionWhenBuildFormSubsequentPaymentThrows() throws JsonProcessingException {
        UUID correlationUUID = UUID.randomUUID();
        CreateHostedOrderRequestDto requestDto = buildCreateHostedOrderRequestDto(ORDER_REQUEST_CONTRACT_ID);

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
                                        "Test error when calling pspApiV1OrdersBuildPost",
                                        HttpStatus.BAD_REQUEST.value(),
                                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                        null,
                                        objectMapper.writeValueAsBytes(
                                                npgClientErrorResponse("GW0001")
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
                                MOCKED_API_KEY,
                                ORDER_REQUEST_CONTRACT_ID
                        )
                )
                .expectError(NpgResponseException.class)
                .verify();
    }

    @Test
    void shouldPropagateErrorCodesWhenBuildFormThrows() throws JsonProcessingException {
        UUID correlationUUID = UUID.randomUUID();
        CreateHostedOrderRequestDto requestDto = buildCreateHostedOrderRequestDto(null);

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
                                        "Test error when calling pspApiV1OrdersBuildPost",
                                        HttpStatus.BAD_REQUEST.value(),
                                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                        null,
                                        objectMapper.writeValueAsBytes(
                                                npgClientErrorResponse("GW0001")
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
    void shouldPropagateErrorCodesWhenBuildFormSubsequentPaymentThrows() throws JsonProcessingException {
        UUID correlationUUID = UUID.randomUUID();
        CreateHostedOrderRequestDto requestDto = buildCreateHostedOrderRequestDto(ORDER_REQUEST_CONTRACT_ID);

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
                                        "Test error when calling pspApiV1OrdersBuildPost",
                                        HttpStatus.BAD_REQUEST.value(),
                                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                        null,
                                        objectMapper.writeValueAsBytes(
                                                npgClientErrorResponse("GW0001")
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
                                MOCKED_API_KEY,
                                ORDER_REQUEST_CONTRACT_ID
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
                        SESSION_ID,
                        MOCKED_API_KEY
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
                        SESSION_ID,
                        MOCKED_API_KEY
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
                                                npgClientErrorResponse("GW0001")
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
                        SESSION_ID,
                        MOCKED_API_KEY
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
                                                npgClientErrorResponse("GW0001")
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
                                && npgResponseException.getErrors().equals(List.of("GW0001"))
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
                                REFUND_DESCRIPTION
                        )
                )
                .expectNext(refundResponseDto)
                .verifyComplete();
    }

    @Test
    void shouldRefundPaymentGivenValidNpgSessionWithNullDescription() {
        RefundResponseDto refundResponseDto = buildTestRefundResponseDto();

        UUID correlationUUID = UUID.randomUUID();
        RefundRequestDto refundRequestDto = buildRefundRequestDtoNullDescription();

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
                                                npgClientErrorResponse("GW0001")
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
                                && npgResponseException.getErrors().equals(List.of("GW0001"))
                                && npgResponseException.getStatusCode().get().equals(HttpStatus.BAD_REQUEST)
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
                                                npgClientErrorResponse("GW0001")
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
                                REFUND_DESCRIPTION
                        )
                )
                .expectErrorMatches(
                        e -> e instanceof NpgResponseException npgResponseException
                                && npgResponseException.getErrors().equals(List.of("GW0001"))
                                && npgResponseException.getStatusCode().get().equals(HttpStatus.BAD_REQUEST)
                )
                .verify();
    }

    @Test
    void shouldPropagateErrorCodesWhileRefundPaymentWithNullDescription() throws JsonProcessingException {
        UUID correlationUUID = UUID.randomUUID();
        RefundRequestDto refundRequestDto = buildRefundRequestDtoNullDescription();

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
                                                npgClientErrorResponse("GW0001")
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
                                && npgResponseException.getErrors().equals(List.of("GW0001"))
                                && npgResponseException.getStatusCode().get().equals(HttpStatus.BAD_REQUEST)
                )
                .verify();
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                    "it",
                    "en",
                    "de",
                    "sl",
                    "fr"
            }
    )
    void shouldPerformOrderBuildForApmWithPayActionAndTransactionAmount(String input) {

        String ISO_639_3_lang = ORDER_REQUEST_LANGUAGE_ITA;
        if (input != null) {
            ISO_639_3_lang = LANG_MAP.getOrDefault(input, ORDER_REQUEST_LANGUAGE_ITA);
        }

        FieldsDto fieldsDto = buildTestFieldsDtoForSubsequentPayment();
        Integer transactionTotalAmount = 1000;
        UUID correlationUUID = UUID.randomUUID();
        CreateHostedOrderRequestDto requestDto = buildCreateHostedOrderRequestDto(
                ORDER_REQUEST_CONTRACT_ID,
                transactionTotalAmount,
                ISO_639_3_lang
        );

        Mockito.when(
                paymentServicesApi.pspApiV1OrdersBuildPost(
                        correlationUUID,
                        MOCKED_API_KEY,
                        requestDto
                )
        ).thenReturn(Mono.just(fieldsDto));

        StepVerifier
                .create(
                        npgClient.buildFormForPayment(
                                correlationUUID,
                                URI.create(MERCHANT_URL),
                                URI.create(RESULT_URL),
                                URI.create(NOTIFICATION_URL),
                                URI.create(CANCEL_URL),
                                ORDER_REQUEST_ORDER_ID,
                                ORDER_REQUEST_CUSTOMER_ID,
                                NpgClient.PaymentMethod.CARDS,
                                MOCKED_API_KEY,
                                ORDER_REQUEST_CONTRACT_ID,
                                transactionTotalAmount,
                                input
                        )
                )
                .expectNext(fieldsDto)
                .verifyComplete();
    }

    @Test
    void shouldGetStateOfPayment() {
        UUID correlationUUID = UUID.randomUUID();

        StateResponseDto stateResponseDto = new StateResponseDto().state(WorkflowStateDto.PAYMENT_COMPLETE);

        Mockito.when(
                paymentServicesApi.pspApiV1BuildStateGet(
                        correlationUUID,
                        SESSION_ID,
                        MOCKED_API_KEY
                )
        ).thenReturn(Mono.just(stateResponseDto));

        StepVerifier
                .create(
                        npgClient.getState(
                                correlationUUID,
                                SESSION_ID,
                                MOCKED_API_KEY
                        )
                )
                .expectNext(stateResponseDto)
                .verifyComplete();

    }

    @Test
    void shouldGetStateOfOrder() {
        UUID correlationUUID = UUID.randomUUID();

        final var response = new OrderResponseDto()
                .orderStatus(
                        new OrderStatusDto()
                                .authorizedAmount("123")
                                .capturedAmount("123")
                                .lastOperationType(OperationTypeDto.REFUND)
                                .lastOperationTime("2022-09-01T01:20:00.001Z")
                )
                .addOperationsItem(
                        new OperationDto()
                                .orderId("order-id")
                                .operationId("operation-id")
                                .operationAmount("123")
                                .operationAmount("123")
                                .paymentMethod(PaymentMethodDto.CARD)
                                .channel(ChannelTypeDto.ECOMMERCE)
                                .operationType(OperationTypeDto.REFUND)
                                .operationResult(OperationResultDto.EXECUTED)
                )
                .links(List.of());

        Mockito.when(
                paymentServicesApi.pspApiV1OrdersOrderIdGet(
                        correlationUUID,
                        ORDER_REQUEST_ORDER_ID,
                        MOCKED_API_KEY
                )
        ).thenReturn(Mono.just(response));

        StepVerifier
                .create(
                        npgClient.getOrder(
                                correlationUUID,
                                MOCKED_API_KEY,
                                ORDER_REQUEST_ORDER_ID
                        )
                )
                .expectNext(response)
                .verifyComplete();

    }

    @Test
    void shouldPropagateErrorCodesWhileGetState() throws JsonProcessingException {
        UUID correlationUUID = UUID.randomUUID();

        Mockito.when(
                paymentServicesApi.pspApiV1BuildStateGet(
                        correlationUUID,
                        SESSION_ID,
                        MOCKED_API_KEY
                )
        )
                .thenReturn(
                        Mono.error(
                                new WebClientResponseException(
                                        "Error while invoke method for get state",
                                        HttpStatus.BAD_REQUEST.value(),
                                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                        null,
                                        objectMapper.writeValueAsBytes(
                                                npgClientErrorResponse("GW0001")
                                        ),
                                        null
                                )
                        )
                );

        StepVerifier
                .create(
                        npgClient.getState(
                                correlationUUID,
                                SESSION_ID,
                                MOCKED_API_KEY
                        )
                )
                .expectErrorMatches(
                        e -> e instanceof NpgResponseException npgResponseException
                                && npgResponseException.getErrors().equals(List.of("GW0001"))
                                && npgResponseException.getStatusCode().get().equals(HttpStatus.BAD_REQUEST)
                )
                .verify();
    }

    @Test
    void shouldPropagateErrorForUnparsableErrorResponse() {
        UUID correlationUUID = UUID.randomUUID();

        Mockito.when(
                paymentServicesApi.pspApiV1BuildStateGet(
                        correlationUUID,
                        SESSION_ID,
                        MOCKED_API_KEY
                )
        )
                .thenReturn(
                        Mono.error(
                                new WebClientResponseException(
                                        "Error while invoke method for get state",
                                        HttpStatus.BAD_REQUEST.value(),
                                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                        null,
                                        "error".getBytes(StandardCharsets.UTF_8),
                                        null
                                )
                        )
                );

        StepVerifier
                .create(
                        npgClient.getState(
                                correlationUUID,
                                SESSION_ID,
                                MOCKED_API_KEY
                        )
                )
                .expectErrorMatches(
                        e -> {
                            NpgResponseException npgResponseException = (NpgResponseException) e;
                            assertTrue(npgResponseException.getErrors().isEmpty());
                            assertEquals(HttpStatus.BAD_REQUEST, npgResponseException.getStatusCode().get());
                            return true;

                        }

                )
                .verify();
    }

    @Test
    void shouldPropagateErrorForGenericExceptionThrownFromClient() {
        UUID correlationUUID = UUID.randomUUID();

        Mockito.when(
                paymentServicesApi.pspApiV1BuildStateGet(
                        correlationUUID,
                        SESSION_ID,
                        MOCKED_API_KEY
                )
        )
                .thenReturn(
                        Mono.error(
                                new NullPointerException("Error while invoke method for get state")
                        )
                );

        StepVerifier
                .create(
                        npgClient.getState(
                                correlationUUID,
                                SESSION_ID,
                                MOCKED_API_KEY
                        )
                )
                .expectErrorMatches(
                        e -> {
                            NpgResponseException npgResponseException = (NpgResponseException) e;
                            assertTrue(npgResponseException.getErrors().isEmpty());
                            assertTrue(npgResponseException.getStatusCode().isEmpty());
                            return true;
                        }

                )
                .verify();
    }

    private static ClientErrorDto npgClientErrorResponse(String gatewayError) {
        return new ClientErrorDto()
                .errors(
                        List.of(
                                new ErrorsInnerDto()
                                        .code(gatewayError)
                                        .description("Error %s".formatted(gatewayError))
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
                .currency(CURRENCY)
                .description(REFUND_DESCRIPTION);
    }

    private RefundRequestDto buildRefundRequestDtoNullDescription() {
        return new RefundRequestDto()
                .amount(AMOUNT)
                .currency(CURRENCY);
    }

    private CreateHostedOrderRequestDto buildCreateHostedOrderRequestDto(String contractId) {
        return buildCreateHostedOrderRequestDto(contractId, null, null);
    }

    private CreateHostedOrderRequestDto buildCreateHostedOrderRequestDto(
                                                                         String contractId,
                                                                         Integer amount,
                                                                         String language
    ) {
        if (language == null) {
            language = ORDER_REQUEST_LANGUAGE_ITA;
        }

        return new CreateHostedOrderRequestDto()
                .version(ORDER_REQUEST_VERSION)
                .merchantUrl(MERCHANT_URL)
                .order(
                        new OrderDto()
                                .orderId(ORDER_REQUEST_ORDER_ID)
                                .amount(Optional.ofNullable(amount).map(Objects::toString).orElse(ORDER_REQUEST_PAY))
                                .currency(ORDER_REQUEST_CURRENCY_EUR)
                                .customerId(ORDER_REQUEST_CUSTOMER_ID)
                )
                .paymentSession(
                        new PaymentSessionDto()
                                .paymentService(ORDER_REQUEST_PAYMENT_SERVICE_CARDS)
                                .amount(Optional.ofNullable(amount).map(Objects::toString).orElse(ORDER_REQUEST_PAY))
                                .actionType(ActionTypeDto.PAY)
                                .language(language)
                                .cancelUrl(CANCEL_URL)
                                .notificationUrl(NOTIFICATION_URL)
                                .resultUrl(RESULT_URL)
                                .recurrence(
                                        contractId != null ? new RecurringSettingsDto()
                                                .action(RecurringActionDto.SUBSEQUENT_PAYMENT)
                                                .contractType(RecurringContractTypeDto.CIT)
                                                .contractId(ORDER_REQUEST_CONTRACT_ID)
                                                : null
                                )
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

    private FieldsDto buildTestFieldsDtoForSubsequentPayment() {
        return new FieldsDto()
                .sessionId(SESSION_ID)
                .state(WorkflowStateDto.READY_FOR_PAYMENT);
    }

    private FieldsDto buildTestFieldsDtoForSubsequentPaymentApm() {
        return new FieldsDto()
                .sessionId(SESSION_ID)
                .state(WorkflowStateDto.REDIRECTED_TO_EXTERNAL_DOMAIN)
                .url("http://external-domain/redirectionUrl");
    }

    private StateResponseDto buildTestRetrieveStateResponseDto() {
        return new StateResponseDto()
                .url("https://iframe-gdi")
                .state(WorkflowStateDto.REDIRECTED_TO_EXTERNAL_DOMAIN);
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
