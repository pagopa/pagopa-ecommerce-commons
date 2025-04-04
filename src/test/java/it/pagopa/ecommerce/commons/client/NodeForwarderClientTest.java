package it.pagopa.ecommerce.commons.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import it.pagopa.ecommerce.commons.exceptions.NodeForwarderClientException;
import it.pagopa.ecommerce.commons.generated.nodeforwarder.v1.api.ProxyApi;
import okhttp3.Headers;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class NodeForwarderClientTest {

    private record TestRequest(
            String testRequestField
    ) {
    }

    private record TestResponse(
            String testResponseField
    ) {
    }

    private final ProxyApi proxyApi = Mockito.mock(ProxyApi.class);

    private NodeForwarderClient<TestRequest, TestResponse> nodeForwarderClient = new NodeForwarderClient<>(
            proxyApi
    );

    private static MockWebServer mockWebServer;

    @BeforeEach
    public void beforeAll() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        System.out.printf("Mock web server listening on %s:%s%n", mockWebServer.getHostName(), mockWebServer.getPort());

    }

    @AfterEach
    public void afterAll() throws IOException {
        mockWebServer.shutdown();
        System.out.println("Mock web server stopped");
    }

    @Test
    void shouldProxyRequestSuccessfullyRetrievingDefaultPortForHttpsUrl() {
        // pre-requisites
        String requestId = UUID.randomUUID().toString();
        TestRequest testRequest = new TestRequest("test");
        URI proxyTo = URI.create("https://localhost/test/request");
        String expectedHostHeader = "localhost";
        int expectedPortHeader = 443;
        String expectedPathRequest = "/test/request";
        String expectedPayload = "{\"testRequestField\":\"test\"}";
        NodeForwarderClient.NodeForwarderResponse<TestResponse> expectedResponse = new NodeForwarderClient.NodeForwarderResponse<>(
                new TestResponse("123"),
                Optional.of(requestId)
        );
        given(proxyApi.forwardWithHttpInfo(any(), any(), any(), any(), any())).willReturn(
                Mono.just(ResponseEntity.ok().header("X-Request-Id", requestId).body("{\"testResponseField\":\"123\"}"))
        );
        // test
        StepVerifier
                .create(nodeForwarderClient.proxyRequest(testRequest, proxyTo, requestId, TestResponse.class))
                .expectNext(expectedResponse)
                .verifyComplete();
        verify(proxyApi, times(1)).forwardWithHttpInfo(
                expectedHostHeader,
                expectedPortHeader,
                expectedPathRequest,
                requestId,
                expectedPayload
        );
    }

    @Test
    void shouldProxyRequestSuccessfullyUsingCustomPort() {
        // pre-requisites
        String requestId = UUID.randomUUID().toString();
        TestRequest testRequest = new TestRequest("test");
        URI proxyTo = URI.create("http://localhost:123/test/request");
        String expectedHostHeader = "localhost";
        int expectedPortHeader = 123;
        String expectedPathRequest = "/test/request";
        String expectedPayload = "{\"testRequestField\":\"test\"}";
        NodeForwarderClient.NodeForwarderResponse<TestResponse> expectedResponse = new NodeForwarderClient.NodeForwarderResponse<>(
                new TestResponse("123"),
                Optional.of(requestId)
        );
        given(proxyApi.forwardWithHttpInfo(any(), any(), any(), any(), any())).willReturn(
                Mono.just(ResponseEntity.ok().header("X-Request-Id", requestId).body("{\"testResponseField\":\"123\"}"))
        );
        // test
        StepVerifier
                .create(nodeForwarderClient.proxyRequest(testRequest, proxyTo, requestId, TestResponse.class))
                .expectNext(expectedResponse)
                .verifyComplete();
        verify(proxyApi, times(1)).forwardWithHttpInfo(
                expectedHostHeader,
                expectedPortHeader,
                expectedPathRequest,
                requestId,
                expectedPayload
        );
    }

    @Test
    void shouldHandleErrorDeserializingResponse() {
        // pre-requisites
        String requestId = UUID.randomUUID().toString();
        TestRequest testRequest = new TestRequest("test");
        URI proxyTo = URI.create("http://localhost:123/test/request");
        String expectedHostHeader = "localhost";
        int expectedPortHeader = 123;
        String expectedPathRequest = "/test/request";
        String expectedPayload = "{\"testRequestField\":\"test\"}";
        given(proxyApi.forwardWithHttpInfo(any(), any(), any(), any(), any())).willReturn(
                Mono.just(ResponseEntity.ok().header("X-Request-Id", requestId).body("{}"))
        );
        // test
        StepVerifier
                .create(nodeForwarderClient.proxyRequest(testRequest, proxyTo, requestId, TestResponse.class))
                .expectErrorMatches(ex -> {
                    assertEquals("Error deserializing body", ex.getMessage());
                    assertTrue(ex.getCause() instanceof JsonProcessingException);
                    return true;
                })
                .verify();
        verify(proxyApi, times(1)).forwardWithHttpInfo(
                expectedHostHeader,
                expectedPortHeader,
                expectedPathRequest,
                requestId,
                expectedPayload
        );
    }

    @Test
    void shouldSendRequestToForwarderWithAllRequiredHeaders() throws Exception {
        // assertions
        String requestId = UUID.randomUUID().toString();
        String apiKey = "apiKey";
        NodeForwarderClient<TestRequest, TestResponse> client = new NodeForwarderClient<>(
                apiKey,
                "http://%s:%s".formatted(mockWebServer.getHostName(), mockWebServer.getPort()),
                10000,
                10000
        );
        TestRequest testRequest = new TestRequest("test");
        URI proxyTo = URI.create("http://localhost:123/test/request");
        mockWebServer.enqueue(
                new MockResponse()
                        .addHeader("X-Request-Id", requestId)
                        .setBody("{\"testResponseField\":\"123\"}")
                        .setResponseCode(200)
        );
        NodeForwarderClient.NodeForwarderResponse<TestResponse> expectedResponse = new NodeForwarderClient.NodeForwarderResponse<>(
                new TestResponse("123"),
                Optional.of(requestId)
        );
        // test
        StepVerifier.create(
                client.proxyRequest(
                        testRequest,
                        proxyTo,
                        requestId,
                        TestResponse.class
                )
        )
                .expectNext(expectedResponse)
                .verifyComplete();
        // assertions
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        Headers requestHeaders = recordedRequest.getHeaders();
        assertEquals(requestId, requestHeaders.get("x-request-id"));
        assertEquals("localhost", requestHeaders.get("x-host-url"));
        assertEquals("123", requestHeaders.get("x-host-port"));
        assertEquals("/test/request", requestHeaders.get("x-host-path"));
        assertEquals(apiKey, requestHeaders.get("Ocp-Apim-Subscription-Key"));

    }

    @Test
    void shouldHandleMissingXRequestIdResponseHeader() {
        // assertions
        String requestId = UUID.randomUUID().toString();
        NodeForwarderClient<TestRequest, TestResponse> client = new NodeForwarderClient<>(
                "apiKey",
                "http://%s:%s".formatted(mockWebServer.getHostName(), mockWebServer.getPort()),
                10000,
                10000
        );
        TestRequest testRequest = new TestRequest("test");
        URI proxyTo = URI.create("http://localhost:123/test/request");
        mockWebServer.enqueue(
                new MockResponse()
                        .setBody("{\"testResponseField\":\"123\"}")
                        .setResponseCode(200)
        );
        NodeForwarderClient.NodeForwarderResponse<TestResponse> expectedResponse = new NodeForwarderClient.NodeForwarderResponse<>(
                new TestResponse("123"),
                Optional.empty()
        );
        // test
        StepVerifier.create(
                client.proxyRequest(
                        testRequest,
                        proxyTo,
                        requestId,
                        TestResponse.class
                )
        )
                .expectNext(expectedResponse)
                .verifyComplete();
    }

    @Test
    void shouldHandleErrorResponseFromForwarder() {
        // assertions
        String requestId = UUID.randomUUID().toString();
        NodeForwarderClient<TestRequest, TestResponse> client = new NodeForwarderClient<>(
                "apiKey",
                "http://%s:%s".formatted(mockWebServer.getHostName(), mockWebServer.getPort()),
                10000,
                10000
        );
        TestRequest testRequest = new TestRequest("test");
        URI proxyTo = URI.create("http://localhost:123/test/request");
        mockWebServer.enqueue(
                new MockResponse()
                        .setBody("error")
                        .setResponseCode(400)
        );
        // test
        StepVerifier.create(
                client.proxyRequest(
                        testRequest,
                        proxyTo,
                        requestId,
                        TestResponse.class
                )
        )
                .expectError(NodeForwarderClientException.class)
                .verify();
    }

}
