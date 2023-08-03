package it.pagopa.ecommerce.commons.client;

import it.pagopa.ecommerce.commons.exceptions.NpgResponseException;
import it.pagopa.ecommerce.commons.generated.npg.v1.api.PaymentServicesApi;
import it.pagopa.ecommerce.commons.generated.npg.v1.dto.CreateHostedOrderRequestDto;
import it.pagopa.ecommerce.commons.generated.npg.v1.dto.FieldsDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotNull;
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
            @NotNull PaymentServicesApi paymentServicesApi,
            @NotNull String npgKey
    ) {
        this.paymentServicesApi = paymentServicesApi;
        this.paymentServicesApi.getApiClient().setApiKey(npgKey);
    }

    /**
     * method to invoke the orders/build api
     *
     * @param createHostedOrderRequestDto the request to create the session
     * @param correlationId               the unique id to identify the rest api
     *                                    invocation
     * @return An object containing sessionId, sessionToken and the fields list to
     *         show on the client-side
     */
    public Mono<FieldsDto> buildOrders(
                                       @NotNull UUID correlationId,
                                       @NotNull CreateHostedOrderRequestDto createHostedOrderRequestDto
    ) {

        return paymentServicesApi.apiOrdersBuildPost(
                correlationId,
                createHostedOrderRequestDto
        ).doOnError(
                WebClientResponseException.class,
                e -> log.info(
                        "Got bad response from npg-service [HTTP {}]",
                        e.getStatusCode()
                )
        )
                .onErrorMap(
                        err -> new NpgResponseException("Error while invoke method for build order", err)
                );

    }

}
