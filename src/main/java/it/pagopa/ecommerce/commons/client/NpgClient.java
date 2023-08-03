package it.pagopa.ecommerce.commons.client;

import it.pagopa.ecommerce.commons.exceptions.NpgResponseException;
import it.pagopa.generated.ecommerce.npg.v1.api.HostedFieldsApi;
import it.pagopa.generated.ecommerce.npg.v1.api.PaymentServicesApi;
import it.pagopa.generated.ecommerce.npg.v1.dto.CreateHostedOrderRequestDto;
import it.pagopa.generated.ecommerce.npg.v1.dto.FieldsDto;
import it.pagopa.generated.ecommerce.npg.v1.dto.PostMessageDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * <p>
 * NpgClient instance to communicate with npg.
 * </p>
 */
@Slf4j
public class NpgClient {

    /**
     * The npg Api
     */
    private final PaymentServicesApi paymentServicesApi;

    /**
     * Instantiate a npg-client to establish communication via the npg api
     *
     * @param paymentServicesApi the api
     * @param npgKey             the api key
     */
    public NpgClient(
            PaymentServicesApi paymentServicesApi,
            String npgKey
    ) {
        this.paymentServicesApi = paymentServicesApi;
        this.paymentServicesApi.getApiClient().setApiKey(npgKey);
    }

    /**
     * method to invoke the orders/build api
     *
     * @param createHostedOrderRequestDto the request to create the session
     * @return An object containing sessionId, sessionToken and the fields list to
     *         show on the client-side
     */
    public Mono<FieldsDto> buildOrders(
                                       UUID correlationId,
                                       CreateHostedOrderRequestDto createHostedOrderRequestDto
    ) {

        return paymentServicesApi.apiOrdersBuildPost(
                correlationId,
                createHostedOrderRequestDto
        ).doOnError(
                WebClientResponseException.class,
                e -> log.info(
                        "Got bad response from npg-service [HTTP {}]: {}",
                        e.getStatusCode(),
                        e.getResponseBodyAsString()
                )
        )
                .onErrorMap(
                        err -> new NpgResponseException("Error while invoke method for read psp list")
                );

    }

}
