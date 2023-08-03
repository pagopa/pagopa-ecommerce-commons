package it.pagopa.ecommerce.commons.client;

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

@ExtendWith(MockitoExtension.class)
class NpgClientTests {
    private static final String MOCKED_API_KEY = "mocked-api-key";
    public static final String ORDER_REQUEST_VERSION = "2";
    public static final String MERCHANT_URL = "localhost/merchant";
    public static final String ORDER_REQUEST_ORDER_ID = "orderId";
    public static final String ORDER_REQUEST_AMOUNT = "0";
    public static final String ORDER_REQUEST_CURRENCY_EUR = "EUR";
    public static final String ORDER_REQUEST_CUSTOMER_ID = "customerId";
    public static final String ORDER_REQUEST_PAYMENT_SERVICE_CARDS = "CARDS";
    public static final String ORDER_REQUEST_LANGUAGE_ITA = "ITA";
    public static final String CANCEL_URL = "localhost/cancel";
    public static final String NOTIFICATION_URL = "localhost/notification";
    public static final String RESULT_URL = "localhost/result";
    public static final String SESSION_ID = "sessionId";
    public static final String SECURITY_TOKEN = "securityToken";
    public static final String TEST_1 = "test1";
    public static final String SRC_1 = "src1";
    public static final String PROPERTY_1 = "property1";
    public static final String TYPE_1 = "type1";
    @Mock
    private ApiClient apiClient;
    @Mock
    @Qualifier("npgWebClient")
    private PaymentServicesApi paymentServicesApi;

    private NpgClient npgClient;

    @BeforeEach
    public void init() {
        Mockito.when(paymentServicesApi.getApiClient()).thenReturn(apiClient);
        npgClient = new NpgClient(paymentServicesApi, MOCKED_API_KEY);
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
                        npgClient.buildForms(
                                correlationUUID,
                                URI.create(MERCHANT_URL),
                                URI.create(RESULT_URL),
                                URI.create(NOTIFICATION_URL),
                                URI.create(CANCEL_URL),
                                ORDER_REQUEST_ORDER_ID,
                                ORDER_REQUEST_CUSTOMER_ID
                        )
                )
                .expectNext(fieldsDto)
                .verifyComplete();
    }

    @Test
    void shouldRetrieveFieldsDto() {
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
                .create(npgClient.buildForms(correlationUUID, requestDto))
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
                .create(npgClient.buildForms(correlationUUID, requestDto))
                .expectError(NpgResponseException.class)
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
