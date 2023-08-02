package it.pagopa.ecommerce.commons.client;

import it.pagopa.ecommerce.commons.exceptions.NpgResponseException;
import it.pagopa.generated.ecommerce.npg.v1.api.HostedFieldsApi;
import it.pagopa.generated.ecommerce.npg.v1.dto.CreateHostedOrderRequestDto;
import it.pagopa.generated.ecommerce.npg.v1.dto.PostMessageDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@Slf4j
public class NpgClient {

    @Autowired
    @Qualifier("ecommercePaymentInstrumentsWebClient")
    private HostedFieldsApi hostedFieldsApi;

    public Mono<PostMessageDto> createHostedOrder(String paymentMethodId, CreateHostedOrderRequestDto createHostedOrderRequestDto) {

        return hostedFieldsApi
                .getApiClient()
                .getWebClient()
                .post()
                .uri(
                        uriBuilder -> uriBuilder.build()
                )
                .header("Correlation-Id",UUID
                        .randomUUID().toString())
                .body(Mono.just(createHostedOrderRequestDto), CreateHostedOrderRequestDto.class)
                .retrieve()
                .onStatus(
                        HttpStatus::isError,
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(
                                        errorResponseBody -> Mono.error(
                                                new NpgResponseException(
                                                        clientResponse.statusCode(),
                                                        errorResponseBody
                                                )
                                        )
                                )
                )
                .bodyToMono(PostMessageDto.class)
                .doOnError(
                        ResponseStatusException.class,
                        error -> log.error(
                                "ResponseStatus Error : {}",
                                error
                        )
                )
                .doOnError(
                        Exception.class,
                        (Exception error) -> log.error(
                                "Generic Error : {}",
                                error
                        )
                );
    }

}
