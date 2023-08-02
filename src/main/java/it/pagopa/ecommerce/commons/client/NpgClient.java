package it.pagopa.ecommerce.commons.client;

import it.pagopa.ecommerce.commons.exceptions.NpgResponseException;
import it.pagopa.generated.ecommerce.npg.v1.api.HostedFieldsApi;
import it.pagopa.generated.ecommerce.npg.v1.dto.CreateHostedOrderRequestDto;
import it.pagopa.generated.ecommerce.npg.v1.dto.PostMessageDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * <p>
 * NpgClient instance to communicate with npg.
 * </p>
 */
@Slf4j
public class NpgClient {

    private final HostedFieldsApi hostedFieldsApi;

    private final String npgKey;

    public NpgClient(
            HostedFieldsApi hostedFieldsApi,
            String npgKey
    ) {
        this.hostedFieldsApi = hostedFieldsApi;
        this.npgKey = npgKey;
    }


    /**
     * method to invoke the orders/build api
     *
     * @param createHostedOrderRequestDto the reqeust to create the session
     * @return Object containing sessionId, sessionToken and the fields list
     */
    public Mono<PostMessageDto> createHostedOrder(
                                                  CreateHostedOrderRequestDto createHostedOrderRequestDto
    ) {

        return hostedFieldsApi
                .getApiClient()
                .getWebClient()
                .post()
                .uri(
                        uriBuilder -> uriBuilder.build()
                )
                .header("ocp-apim-subscription-key", npgKey) //TODO Check the name
                .header(
                        "Correlation-Id",
                        UUID
                                .randomUUID().toString()
                )
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
