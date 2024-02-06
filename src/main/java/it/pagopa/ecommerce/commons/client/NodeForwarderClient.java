package it.pagopa.ecommerce.commons.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import it.pagopa.ecommerce.commons.exceptions.NodeForwarderClientException;
import it.pagopa.ecommerce.commons.generated.nodeforwarder.v1.ApiClient;
import it.pagopa.ecommerce.commons.generated.nodeforwarder.v1.api.ProxyApi;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Node forwarder api client implementation
 *
 * @param <T> the request to proxy POJO class type
 * @param <R> the expected response POJO class type
 * @see ProxyApi
 */

public class NodeForwarderClient<T, R> {

    private final String apiKey;
    private final String backendUrl;
    private final int readTimeout;
    private final int connectionTimeout;

    private final Class<R> responseClass;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new Jdk8Module())
            .configure(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES, true)
            .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true)
            .configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

    private final ProxyApi proxyApiClient;

    /**
     * Header that contains unique request id
     */
    private static final String REQUEST_ID_HEADER_VALUE = "X-Request-Id";

    /**
     * Node forwared response body
     *
     * @param response
     * @param requestId
     * @param <R>
     */
    public record NodeForwarderResponse<R> (
            R response,
            Optional<String> requestId
    ) {
    }

    /**
     * Build a new instance for this Node Forwarder Client
     *
     * @param apiKey            the node forwarder api key
     * @param backendUrl        the node forwarder backend URL
     * @param readTimeout       the node forwarder read timeout
     * @param connectionTimeout the node forwarder connection timeout
     * @param responseClass     the expected response POJO class
     */
    public NodeForwarderClient(
            String apiKey,
            String backendUrl,
            int readTimeout,
            int connectionTimeout,
            Class<R> responseClass
    ) {
        this.apiKey = Objects.requireNonNull(apiKey);
        this.backendUrl = Objects.requireNonNull(backendUrl);
        this.readTimeout = readTimeout;
        this.connectionTimeout = connectionTimeout;
        this.responseClass = Objects.requireNonNull(responseClass);
        this.proxyApiClient = initializeClient();

    }

    /**
     * Build a new {@link ProxyApi} that will be used to perform api calls to be
     * forwarded
     *
     * @return the initialized api client instance
     */
    private ProxyApi initializeClient() {
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
                ).baseUrl(backendUrl).build();

        ApiClient apiClient = new ApiClient(
                webClient
        ).setBasePath(backendUrl);
        apiClient.setApiKey(apiKey);
        return new ProxyApi(apiClient);
    }

    /**
     * Proxy the input request to the proxyTo destination
     *
     * @param request   the request to proxy
     * @param proxyTo   the destination URL where proxy request to
     * @param requestId an optional request id that
     * @return the parsed response body or a Mono error with causing error code
     */
    public Mono<NodeForwarderResponse<R>> proxyRequest(
                                                       T request,
                                                       URL proxyTo,
                                                       String requestId
    ) {
        Objects.requireNonNull(request);
        Objects.requireNonNull(proxyTo);
        String requestPayload;
        try {
            requestPayload = objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            return Mono.error(new NodeForwarderClientException("Error serializing request", e));
        }
        String hostName = proxyTo.getHost();
        int port = getPortOrDefault(proxyTo);
        String path = proxyTo.getPath();
        return proxyApiClient.forwardWithHttpInfo(
                hostName,
                port,
                path,
                requestId,
                requestPayload
        ).flatMap(response -> {
            try {
                return Mono.just(
                        new NodeForwarderResponse<>(
                                objectMapper.readValue(response.getBody(), responseClass),
                                Optional.ofNullable(response.getHeaders().getFirst(REQUEST_ID_HEADER_VALUE))
                        )
                );
            } catch (JsonProcessingException e) {
                return Mono.error(new NodeForwarderClientException("Error deserializing response", e));
            }
        });
    }

    private int getPortOrDefault(URL uri) {
        int port = uri.getPort();
        if (port == -1) {
            //port is not defined into URI, assuming default port based on protocol
            port = switch (uri.getProtocol()) {
                case "http" -> 80;
                case "https",
                        default -> 443;
            };
        }
        return port;
    }

}
