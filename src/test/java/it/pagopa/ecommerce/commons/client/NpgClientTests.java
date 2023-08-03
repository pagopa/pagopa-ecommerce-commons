package it.pagopa.ecommerce.commons.client;

import it.pagopa.ecommerce.commons.exceptions.NpgResponseException;
import it.pagopa.generated.ecommerce.npg.v1.ApiClient;
import it.pagopa.generated.ecommerce.npg.v1.api.PaymentServicesApi;
import it.pagopa.generated.ecommerce.npg.v1.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.mockito.ArgumentMatchers.any;

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
    void shouldRetrievePostMessageDto() {
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
