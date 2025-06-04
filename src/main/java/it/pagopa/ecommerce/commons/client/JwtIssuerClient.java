package it.pagopa.ecommerce.commons.client;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import it.pagopa.ecommerce.commons.exceptions.JwtIssuerClientException;
import it.pagopa.ecommerce.commons.generated.jwtissuer.v1.ApiClient;
import it.pagopa.ecommerce.commons.generated.jwtissuer.v1.api.JwtIssuerApi;
import it.pagopa.ecommerce.commons.generated.jwtissuer.v1.dto.CreateTokenRequestDto;
import it.pagopa.ecommerce.commons.generated.jwtissuer.v1.dto.CreateTokenResponseDto;
import it.pagopa.ecommerce.commons.generated.jwtissuer.v1.dto.JWKSResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Jwt issuer client PI implementation
 *
 * @see JwtIssuerApi
 */
@Slf4j
public class JwtIssuerClient {
    /**
     * The claim transactionId
     */
    public static final String TRANSACTION_ID_CLAIM = "transactionId";

    /**
     * The claim orderId
     */
    public static final String ORDER_ID_CLAIM = "orderId";

    /**
     * The claim payment methodId
     */
    public static final String PAYMENT_METHOD_ID_CLAIM = "paymentMethodId";
    /**
     * The claim userId
     */
    public static final String USER_ID_CLAIM = "userId";
    /**
     * Audience for ecommerce
     */
    public static final String ECOMMERCE_AUDIENCE = "ecommerce";
    /**
     * Audience for npg
     */
    public static final String NPG_AUDIENCE = "npg";

    private static final String JWT_ISSUER_LOG_ERROR_MESSAGE = "Got bad response from jwt-issuer-service [HTTP {}]";

    private final JwtIssuerApi jwtIssuerApi;

    /**
     * Instantiate a jwt issuer client to establish communication via the jwt issuer
     * api
     *
     * @param jwtIssuerApi the api
     */
    public JwtIssuerClient(
            @NotNull JwtIssuerApi jwtIssuerApi
    ) {
        this.jwtIssuerApi = jwtIssuerApi;
    }

    private static void logError(WebClientResponseException e) {
        log.info(
                JWT_ISSUER_LOG_ERROR_MESSAGE,
                e.getStatusCode()
        );
    }

    /**
     * Retrieve the keys to validate a jwt token
     *
     * @return An object containing the keys to validate a token
     */
    public Mono<JWKSResponseDto> getKeys() {
        return jwtIssuerApi.getTokenPublicKeys().doOnError(
                WebClientResponseException.class,
                JwtIssuerClient::logError
        )
                .onErrorMap(err -> new JwtIssuerClientException("Error communicating with JWT issuer", err));
    }

    /**
     * Create a valid jwt token. The parameters set the <i>audience</i> of the
     * token, its <i>duration</i> and the <i>privateClaims</i> map of all private
     * claims to set into the token.
     *
     * @param audience      the audience of the token
     * @param duration      the duration of the token
     * @param privateClaims the private claims to set into the token
     *
     * @return An object containing the generated token
     */
    public Mono<CreateTokenResponseDto> createJWTToken(
                                                       String audience,
                                                       Integer duration,
                                                       Map<String, String> privateClaims
    ) {
        return jwtIssuerApi.createJwtToken(
                new CreateTokenRequestDto()
                        .audience(audience)
                        .duration(duration)
                        .privateClaims(privateClaims)
        ).doOnError(
                WebClientResponseException.class,
                JwtIssuerClient::logError
        )
                .onErrorMap(err -> new JwtIssuerClientException("Error communicating with JWT issuer", err));
    }

}
