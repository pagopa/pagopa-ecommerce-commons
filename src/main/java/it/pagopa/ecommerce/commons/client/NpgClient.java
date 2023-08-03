package it.pagopa.ecommerce.commons.client;

import it.pagopa.ecommerce.commons.exceptions.NpgResponseException;
import it.pagopa.ecommerce.commons.generated.npg.v1.api.PaymentServicesApi;
import it.pagopa.ecommerce.commons.generated.npg.v1.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotNull;
import java.net.URI;
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
     * method to invoke the orders/build api in order to start a payment session,
     * retrieve the sessionId and sessionToken and the fields of the form to display
     * in the webview. This method ensures that the request dto for the orders/build
     * api will be built in the right way (it is easy to build it manually with
     * wrong values, e.g. <i>amount</i> or <i>currency</i> as a string can be easily
     * confused).
     *
     * @param correlationId   the unique id to identify the rest api invocation
     * @param merchantUrl     the merchant url of the payment session
     * @param resultUrl       the result url where the user should be redirected at
     *                        the end of the payment session
     * @param notificationUrl the notification url where notify the session
     * @param cancelUrl       the url where the user should be redirected if the
     *                        session is canceled by the user
     * @param orderId         the orderId of the payment session
     * @param customerId      the customerId url of the api
     * @return An object containing sessionId, sessionToken and the fields list to
     *         show on the client-side
     */
    public Mono<FieldsDto> buildForms(
                                      @NotNull UUID correlationId,
                                      @NotNull URI merchantUrl,
                                      @NotNull URI resultUrl,
                                      @NotNull URI notificationUrl,
                                      @NotNull URI cancelUrl,
                                      @NotNull String orderId,
                                      @NotNull String customerId
    ) {
        return buildForms(
                correlationId,
                buildOrderRequestDto(merchantUrl, resultUrl, notificationUrl, cancelUrl, orderId, customerId)
        );
    }

    /**
     * method to invoke the orders/build api in order to start a payment session,
     * retrieve the sessionId and sessionToken and the fields of the form to display
     * in the webview. This method allows to use the original dto to send the request to npg.
     *
     * @param createHostedOrderRequestDto the request to create the session
     * @param correlationId               the unique id to identify the rest api
     *                                    invocation
     * @return An object containing sessionId, sessionToken and the fields list to
     *         show on the client-side
     */
    public Mono<FieldsDto> buildForms(
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

    private CreateHostedOrderRequestDto buildOrderRequestDto(
                                                             URI merchantUrl,
                                                             URI resultUrl,
                                                             URI notificationUrl,
                                                             URI cancelUrl,
                                                             String orderId,
                                                             String customerId
    ) {
        return new CreateHostedOrderRequestDto()
                .version("2")
                .merchantUrl(merchantUrl.toString())
                .order(
                        new OrderDto()
                                .orderId(orderId)
                                .amount("0")
                                .currency("EUR")
                                .customerId(customerId)
                )
                .paymentSession(
                        new PaymentSessionDto()
                                .actionType(ActionTypeDto.VERIFY)
                                .amount("0")
                                .language("ITA")
                                .paymentService("CARDS")
                                .resultUrl(resultUrl.toString())
                                .cancelUrl(cancelUrl.toString())
                                .notificationUrl(notificationUrl.toString())
                );
    }

}
