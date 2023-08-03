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
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class NpgClientTests {
    @Mock
    private ApiClient apiClient;
    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;
    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;
    @Mock
    @Qualifier("npgWebClient")
    private PaymentServicesApi paymentServicesApi;

    private NpgClient npgClient;

    @BeforeEach
    public void init() {
        Mockito.when(paymentServicesApi.getApiClient()).thenReturn(apiClient);
        npgClient = new NpgClient(paymentServicesApi, "xxx");
    }

    @Test
    void shouldRetrieveFieldsDto() {
        FieldsDto fieldsDto = getFieldsDto();

        UUID correlationUUID = UUID.randomUUID();
        CreateHostedOrderRequestDto requestDto = getRequestDto();

        Mockito.when(
                paymentServicesApi.apiOrdersBuildPost(
                        correlationUUID,
                        requestDto
                )
        ).thenReturn(Mono.just(fieldsDto));

        StepVerifier
                .create(npgClient.buildOrders(correlationUUID, requestDto))
                .expectNext(fieldsDto)
                .verifyComplete();
    }

    @Test
    void shouldThrowException() {
        UUID correlationUUID = UUID.randomUUID();
        CreateHostedOrderRequestDto requestDto = getRequestDto();

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
                .create(npgClient.buildOrders(correlationUUID, requestDto))
                .expectError(NpgResponseException.class)
                .verify();
    }

    private CreateHostedOrderRequestDto getRequestDto() {
        return new CreateHostedOrderRequestDto()
                .merchantUrl("localhost/merchant")
                .order(
                        new OrderDto()
                                .orderId("testId")
                                .amount("0")
                                .currency("EUR")
                                .customerId("customerId")
                )
                .paymentSession(
                        new PaymentSessionDto()
                                .paymentService("paymentService")
                                .amount("0").actionType(ActionTypeDto.PAY)
                                .cancelUrl("localhost/cancel")
                                .notificationUrl("localhost/notification")
                                .resultUrl("localhost/result")
                );
    }

    private FieldsDto getFieldsDto() {
        return new FieldsDto()
                .sessionId("sessionId")
                .securityToken("securityToken")
                .fields(
                        List.of(
                                new FieldDto().id("test1").src("src1").propertyClass("property1").type("type1")
                        )
                );
    }

}
