package it.pagopa.ecommerce.commons.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import it.pagopa.ecommerce.commons.generated.nodeforwarder.v1.api.ProxyApi;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
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
            proxyApi,
            TestResponse.class
    );

    private static MockWebServer mockWebServer;

    @BeforeAll
    public static void beforeAll() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start(8080);
        System.out.printf("Mock web server listening on %s:%s%n", mockWebServer.getHostName(), mockWebServer.getPort());

    }

    @AfterAll
    public static void afterAll() throws IOException {
        mockWebServer.shutdown();
        System.out.println("Mock web server stopped");
    }

    @Test
    void shouldProxyRequestSuccessfullyRetrievingDefaultPortForHttpsUrl() throws Exception {
        // pre-requisites
        String requestId = UUID.randomUUID().toString();
        TestRequest testRequest = new TestRequest("test");
        URL proxyTo = URI.create("https://localhost/test/request").toURL();
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
                .create(nodeForwarderClient.proxyRequest(testRequest, proxyTo, requestId))
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
    void shouldProxyRequestSuccessfullyUsingCustomPort() throws Exception {
        // pre-requisites
        String requestId = UUID.randomUUID().toString();
        TestRequest testRequest = new TestRequest("test");
        URL proxyTo = URI.create("http://localhost:123/test/request").toURL();
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
                .create(nodeForwarderClient.proxyRequest(testRequest, proxyTo, requestId))
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
    void shouldHandleErrorDeserializingResponse() throws Exception {
        // pre-requisites
        String requestId = UUID.randomUUID().toString();
        TestRequest testRequest = new TestRequest("test");
        URL proxyTo = URI.create("http://localhost:123/test/request").toURL();
        String expectedHostHeader = "localhost";
        int expectedPortHeader = 123;
        String expectedPathRequest = "/test/request";
        String expectedPayload = "{\"testRequestField\":\"test\"}";
        given(proxyApi.forwardWithHttpInfo(any(), any(), any(), any(), any())).willReturn(
                Mono.just(ResponseEntity.ok().header("X-Request-Id", requestId).body("{}"))
        );
        // test
        StepVerifier
                .create(nodeForwarderClient.proxyRequest(testRequest, proxyTo, requestId))
                .expectErrorMatches(ex -> {
                    assertEquals("Error deserializing response", ex.getMessage());
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
    void shouldBuildApiClientSuccessfully() throws MalformedURLException {
        // assertions
        String requestId = UUID.randomUUID().toString();
        NodeForwarderClient<TestRequest, TestResponse> client = new NodeForwarderClient<>(
                "apiKey",
                "http://%s:%s".formatted(mockWebServer.getHostName(), mockWebServer.getPort()),
                10000,
                10000,
                TestResponse.class
        );
        TestRequest testRequest = new TestRequest("test");
        URL proxyTo = URI.create("http://localhost:123/test/request").toURL();
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
                        requestId
                )
        )
                .expectNext(expectedResponse)
                .verifyComplete();
    }

    @Test
    void shouldHandleMissingXRequestIdResponseHeader() throws MalformedURLException {
        // assertions
        String requestId = UUID.randomUUID().toString();
        NodeForwarderClient<TestRequest, TestResponse> client = new NodeForwarderClient<>(
                "apiKey",
                "http://%s:%s".formatted(mockWebServer.getHostName(), mockWebServer.getPort()),
                10000,
                10000,
                TestResponse.class
        );
        TestRequest testRequest = new TestRequest("test");
        URL proxyTo = URI.create("http://localhost:123/test/request").toURL();
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
                        requestId
                )
        )
                .expectNext(expectedResponse)
                .verifyComplete();
    }

}
