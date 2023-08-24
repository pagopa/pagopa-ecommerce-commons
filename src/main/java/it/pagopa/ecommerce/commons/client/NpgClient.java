package it.pagopa.ecommerce.commons.client;

import it.pagopa.ecommerce.commons.exceptions.NpgResponseException;
import it.pagopa.ecommerce.commons.generated.npg.v1.api.PaymentServicesApi;
import it.pagopa.ecommerce.commons.generated.npg.v1.dto.*;
import lombok.NonNull;
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

    private static final String CREATE_HOSTED_ORDER_REQUEST_VERSION = "2";
    private static final String CREATE_HOSTED_ORDER_REQUEST_VERIFY_AMOUNT = "0";
    private static final String CREATE_HOSTED_ORDER_REQUEST_CURRENCY_EUR = "EUR";
    private static final String CREATE_HOSTED_ORDER_REQUEST_LANGUAGE_ITA = "ITA";

    /**
     * The npg Api
     */
    private final PaymentServicesApi paymentServicesApi;

    /**
     * <p>
     * Enumeration for payment methods which NPG can do payments with.
     * </p>
     * <p>
     * Maps to the `paymentService` field when initiating a payment.
     * </p>
     */
    public enum PaymentMethod {
        /**
         * Credit cards
         */
        CARDS("CARDS"),
        /**
         * PayPal
         */
        PAYPAL("PAYPAL"),
        /**
         * PayPal with Pay in 3 option
         */
        PAYPAL_PAGAIN3("PAYPAL_PAGAIN3"),
        /**
         * GiroPay
         */
        GIROPAY("GIROPAY"),
        /**
         * Ideal
         */
        IDEAL("IDEAL"),
        /**
         * MyBank
         */
        MYBANK("MYBANK"),
        /**
         * Google Pay
         */
        GOOGLEPAY("GOOGLEPAY"),
        /**
         * Apple Pay
         */
        APPLEPAY("APPLEPAY"),
        /**
         * Bancomat Pay
         */
        BANCOMATPAY("BANCOMATPAY"),
        /**
         * Bancontact
         */
        BANCONTACT("BANCONTACT"),
        /**
         * Multibanco
         */
        MULTIBANCO("MULTIBANCO"),
        /**
         * WeChat
         */
        WECHAT("WECHAT"),
        /**
         * AliPay
         */
        ALIPAY("ALIPAY"),
        /**
         * PIS (3DS2 Payment Initiation Service)
         */
        PIS("PIS");

        /**
         * API value for `serviceName`
         */
        public final String serviceName;

        PaymentMethod(String serviceName) {
            this.serviceName = serviceName;
        }

        /**
         * Retrieves a {@link PaymentMethod} by its service name
         *
         * @param serviceName the service name
         * @return the corresponding payment method
         * @throws IllegalArgumentException if no payment method exists for the given
         *                                  service name
         */
        public static PaymentMethod fromServiceName(String serviceName) {
            for (PaymentMethod paymentMethod : PaymentMethod.values()) {
                if (paymentMethod.serviceName.equals(serviceName)) {
                    return paymentMethod;
                }
            }

            throw new IllegalArgumentException("Invalid payment method service name: '%s'".formatted(serviceName));
        }
    }

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
     * @param paymentMethod   the payment method for which the form should be built
     * @return An object containing sessionId, sessionToken and the fields list to
     *         show on the client-side
     */
    /*
     * @formatter:off
     *
     * Warning java:S107 - Methods should not have too many parameters
     * Suppressed because this method wraps the underlying API which has this many parameters
     *
     * @formatter:on
     */
    @SuppressWarnings("java:S107")
    public Mono<FieldsDto> buildForm(
                                     @NotNull UUID correlationId,
                                     @NotNull URI merchantUrl,
                                     @NotNull URI resultUrl,
                                     @NotNull URI notificationUrl,
                                     @NotNull URI cancelUrl,
                                     @NotNull String orderId,
                                     @NotNull String customerId,
                                     @NonNull PaymentMethod paymentMethod
    ) {

        return paymentServicesApi.apiOrdersBuildPost(
                correlationId,
                buildOrderRequestDto(
                        merchantUrl,
                        resultUrl,
                        notificationUrl,
                        cancelUrl,
                        orderId,
                        customerId,
                        paymentMethod
                )
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
                                                             String customerId,
                                                             PaymentMethod paymentMethod
    ) {
        return new CreateHostedOrderRequestDto()
                .version(CREATE_HOSTED_ORDER_REQUEST_VERSION)
                .merchantUrl(merchantUrl.toString())
                .order(
                        new OrderDto()
                                .orderId(orderId)
                                .amount(CREATE_HOSTED_ORDER_REQUEST_VERIFY_AMOUNT)
                                .currency(CREATE_HOSTED_ORDER_REQUEST_CURRENCY_EUR)
                                .customerId(customerId)
                )
                .paymentSession(
                        new PaymentSessionDto()
                                .actionType(ActionTypeDto.VERIFY)
                                .amount(CREATE_HOSTED_ORDER_REQUEST_VERIFY_AMOUNT)
                                .language(CREATE_HOSTED_ORDER_REQUEST_LANGUAGE_ITA)
                                .paymentService(paymentMethod.serviceName)
                                .resultUrl(resultUrl.toString())
                                .cancelUrl(cancelUrl.toString())
                                .notificationUrl(notificationUrl.toString())
                );
    }

}
