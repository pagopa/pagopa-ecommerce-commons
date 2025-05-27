package it.pagopa.ecommerce.commons.client;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.opentelemetry.api.trace.Tracer;
import it.pagopa.ecommerce.commons.exceptions.JwtIssuerClientException;
import it.pagopa.ecommerce.commons.generated.jwtissuer.v1.ApiClient;
import it.pagopa.ecommerce.commons.generated.jwtissuer.v1.api.JwtIssuerApi;
import it.pagopa.ecommerce.commons.generated.jwtissuer.v1.dto.CreateTokenRequestDto;
import it.pagopa.ecommerce.commons.generated.jwtissuer.v1.dto.CreateTokenResponseDto;
import it.pagopa.ecommerce.commons.generated.jwtissuer.v1.dto.JWKSResponseDto;
import it.pagopa.ecommerce.commons.generated.nodeforwarder.v1.api.ProxyApi;
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

    /**
     * Instantiate a jwt issuer client to establish communication via the jwt issuer
     * api, and initialize it using the backend uri <i>jwtIssuerBackendUrl</i>, the
     * <i>readTimeout</i> and the <i>connectionTimeout</i>
     *
     * @param jwtIssuerBackendUrl the api
     * @param readTimeout         the api
     * @param connectionTimeout   the api
     */
    public JwtIssuerClient(
            @NotNull String jwtIssuerBackendUrl,
            @NotNull Integer readTimeout,
            @NotNull Integer connectionTimeout
    ) {
        this.jwtIssuerApi = initializeClient(
                jwtIssuerBackendUrl,
                readTimeout,
                connectionTimeout
        );
    }

    /**
     * Build a new {@link JwtIssuerApi} that will be used to perform api calls to be
     * forwarded
     *
     * @return the initialized api client instance
     */
    private JwtIssuerApi initializeClient(
                                          @NotNull String backendUrl,
                                          @NotNull Integer readTimeout,
                                          @NotNull Integer connectionTimeout
    ) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeout)
                .doOnConnected(
                        connection -> connection.addHandlerLast(
                                new ReadTimeoutHandler(
                                        readTimeout,
                                        TimeUnit.MILLISECONDS
                                )
                        )
                );

        WebClient webClient = ApiClient.buildWebClientBuilder()
                .clientConnector(
                        new ReactorClientHttpConnector(httpClient)
                ).baseUrl(backendUrl)
                .build();

        ApiClient apiClient = new ApiClient(
                webClient
        ).setBasePath(backendUrl);
        return new JwtIssuerApi(apiClient);
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
