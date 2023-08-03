package it.pagopa.ecommerce.commons.client;

import it.pagopa.ecommerce.commons.exceptions.NpgResponseException;
import it.pagopa.generated.ecommerce.npg.v1.ApiClient;
import it.pagopa.generated.ecommerce.npg.v1.api.HostedFieldsApi;
import it.pagopa.generated.ecommerce.npg.v1.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.function.Function;
import java.util.function.Predicate;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class NpgClientTests {
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
    private HostedFieldsApi hostedFieldsApi;

    private NpgClient npgClient;

    @BeforeEach
    public void init() {
        npgClient = new NpgClient(hostedFieldsApi, "xxx");
    }

    @Test
    void shouldRetrievePostMessageDto() {
        PostMessageDto postMessageDto = getPostMessageDto();
        Mockito.when(hostedFieldsApi.getApiClient()).thenReturn(apiClient);
        Mockito.when(apiClient.getWebClient()).thenReturn(webClient);
        Mockito.when(webClient.post()).thenReturn(requestBodyUriSpec);
        Mockito.when(requestBodyUriSpec.uri(any(Function.class))).thenReturn(requestBodySpec);
        Mockito.when(requestBodySpec.header(any(), any())).thenReturn(requestBodySpec);
        Mockito.when(requestBodySpec.body(any(Publisher.class), any(Class.class))).thenReturn(requestHeadersSpec);
        Mockito.when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        Mockito.when(responseSpec.onStatus(any(Predicate.class), any(Function.class))).thenReturn(responseSpec);
        Mockito.when(responseSpec.bodyToMono(PostMessageDto.class)).thenReturn(Mono.just(postMessageDto));

        StepVerifier
                .create(npgClient.createHostedOrder(new CreateHostedOrderRequestDto()))
                .expectNext(postMessageDto)
                .verifyComplete();
    }

    private PostMessageDto getPostMessageDto() {
        return new PostMessageDto()
                .event(PostMessageEventDto.SUCCESS)
                .fieldSet(
                        new FieldsDto().addFieldsItem(
                                new FieldDto().id("test1").src("src1").propertyClass("property1").type("type1")
                        )
                )
                .gdiStatus(new GDIStatusDto().gdiUrl("gdiUrl").operation(new OperationDto().operationAmount("0")))
                .state(WorkflowStateDto.READY_FOR_PAYMENT);
    }

    @Test
    void shouldReturnResponseStatusException() {
        Mockito.when(hostedFieldsApi.getApiClient()).thenReturn(apiClient);
        Mockito.when(apiClient.getWebClient()).thenReturn(webClient);
        Mockito.when(webClient.post()).thenReturn(requestBodyUriSpec);
        Mockito.when(requestBodyUriSpec.uri(any(Function.class))).thenReturn(requestBodySpec);
        Mockito.when(requestBodySpec.header(any(), any())).thenReturn(requestBodySpec);
        Mockito.when(requestBodySpec.body(any(Publisher.class), any(Class.class))).thenReturn(requestHeadersSpec);
        Mockito.when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        Mockito.when(responseSpec.onStatus(any(Predicate.class), any(Function.class))).thenReturn(responseSpec);
        Mockito.when(responseSpec.bodyToMono(PostMessageDto.class))
                .thenReturn(Mono.error(new NpgResponseException(HttpStatus.INTERNAL_SERVER_ERROR, "error_test")));

        StepVerifier
                .create(npgClient.createHostedOrder(new CreateHostedOrderRequestDto()))
                .expectError(ResponseStatusException.class);
    }

    @Test
    void shouldReturnException() {
        Mockito.when(hostedFieldsApi.getApiClient()).thenReturn(apiClient);
        Mockito.when(apiClient.getWebClient()).thenReturn(webClient);
        Mockito.when(webClient.post()).thenReturn(requestBodyUriSpec);
        Mockito.when(requestBodyUriSpec.uri(any(Function.class))).thenReturn(requestBodySpec);
        Mockito.when(requestBodySpec.header(any(), any())).thenReturn(requestBodySpec);
        Mockito.when(requestBodySpec.body(any(Publisher.class), any(Class.class))).thenReturn(requestHeadersSpec);
        Mockito.when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        Mockito.when(responseSpec.onStatus(any(Predicate.class), any(Function.class))).thenReturn(responseSpec);
        Mockito.when(responseSpec.bodyToMono(PostMessageDto.class)).thenReturn(Mono.error(new Exception()));

        StepVerifier
                .create(npgClient.createHostedOrder(new CreateHostedOrderRequestDto()))
                .expectError(Exception.class);
    }

}
