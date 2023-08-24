package it.pagopa.ecommerce.commons.client;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;
import it.pagopa.ecommerce.commons.exceptions.NpgResponseException;
import it.pagopa.ecommerce.commons.generated.npg.v1.ApiClient;
import it.pagopa.ecommerce.commons.generated.npg.v1.api.PaymentServicesApi;
import it.pagopa.ecommerce.commons.generated.npg.v1.dto.*;
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
    private static final String ORDER_REQUEST_AMOUNT = "0";
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
    @Mock
    private ApiClient apiClient;
    @Mock
    @Qualifier("npgWebClient")
    private PaymentServicesApi paymentServicesApi;

    @Mock
    private Tracer tracer;

    private NpgClient npgClient;

    @BeforeEach
    public void init() {
        Mockito.when(paymentServicesApi.getApiClient()).thenReturn(apiClient);

        SpanBuilder spanBuilder = Mockito.mock(SpanBuilder.class);
        Mockito.when(spanBuilder.setParent(any())).thenReturn(spanBuilder);
        Mockito.when(spanBuilder.setAttribute(anyString(), anyString())).thenReturn(spanBuilder);
        Mockito.when(spanBuilder.startSpan()).thenReturn(Span.getInvalid());

        Mockito.when(tracer.spanBuilder(anyString())).thenReturn(spanBuilder);

        npgClient = new NpgClient(paymentServicesApi, MOCKED_API_KEY, tracer);
    }

    @Test
    void shouldRetrieveFieldsDtoUsingExplicitParameters() {
        FieldsDto fieldsDto = buildTestFieldsDto();

        UUID correlationUUID = UUID.randomUUID();
        CreateHostedOrderRequestDto requestDto = buildCreateHostedOrderRequestDto();

        Mockito.when(
                paymentServicesApi.apiOrdersBuildPost(
                        correlationUUID,
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
                                NpgClient.PaymentMethod.CARDS
                        )
                )
                .expectNext(fieldsDto)
                .verifyComplete();
    }

    @Test
    void shouldThrowException() {
        UUID correlationUUID = UUID.randomUUID();
        CreateHostedOrderRequestDto requestDto = buildCreateHostedOrderRequestDto();

        Mockito.when(
                paymentServicesApi.apiOrdersBuildPost(
                        correlationUUID,
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
                                        null,
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
                                NpgClient.PaymentMethod.CARDS
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
                paymentServicesApi.apiBuildCardDataGet(
                        correlationUUID,
                        SESSION_ID
                )
        ).thenReturn(Mono.just(expectedResponse));

        StepVerifier
                .create(
                        npgClient.getCardData(
                                correlationUUID,
                                SESSION_ID
                        )
                )
                .expectNext(expectedResponse)
                .verifyComplete();
    }

    @Test
    void shouldThrowExceptionWhileRetrieveCardData() {
        UUID correlationUUID = UUID.randomUUID();

        Mockito.when(
                paymentServicesApi.apiBuildCardDataGet(
                        correlationUUID,
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
                                        null,
                                        null
                                )
                        )
                );

        StepVerifier
                .create(
                        npgClient.getCardData(
                                correlationUUID,
                                SESSION_ID
                        )
                )
                .expectErrorMatches(
                        e -> e instanceof NpgResponseException npgResponseException
                                && npgResponseException.getMessage()
                                        .equals("Error while invoke method for get card data")
                )
                .verify();
    }

    private CreateHostedOrderRequestDto buildCreateHostedOrderRequestDto() {
        return new CreateHostedOrderRequestDto()
                .version(ORDER_REQUEST_VERSION)
                .merchantUrl(MERCHANT_URL)
                .order(
                        new OrderDto()
                                .orderId(ORDER_REQUEST_ORDER_ID)
                                .amount(ORDER_REQUEST_AMOUNT)
                                .currency(ORDER_REQUEST_CURRENCY_EUR)
                                .customerId(ORDER_REQUEST_CUSTOMER_ID)
                )
                .paymentSession(
                        new PaymentSessionDto()
                                .paymentService(ORDER_REQUEST_PAYMENT_SERVICE_CARDS)
                                .amount(ORDER_REQUEST_AMOUNT)
                                .actionType(ActionTypeDto.VERIFY)
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

}
